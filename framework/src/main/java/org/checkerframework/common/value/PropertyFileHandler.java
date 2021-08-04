package org.checkerframework.common.value;

import static org.checkerframework.common.value.ValueAnnotatedTypeFactory.getStringValues;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic.Kind;
import org.checkerframework.common.value.qual.PropertyFile;
import org.checkerframework.common.value.qual.PropertyFileBottom;
import org.checkerframework.common.value.qual.PropertyFileUnknown;
import org.checkerframework.common.value.qual.StringVal;
import org.checkerframework.dataflow.analysis.FlowExpressions;
import org.checkerframework.dataflow.analysis.FlowExpressions.Receiver;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;

/**
 * Utility class for handling {@link java.util.Properties#getProperty(String)} and {@link
 * java.util.Properties#getProperty(String, String)} invocations.
 */
public class PropertyFileHandler {

    /** The processing environment. */
    protected final ProcessingEnvironment env;

    /** The factory for constructing and looking up types. */
    protected final ValueAnnotatedTypeFactory factory;

    /** The checker, used for issuing diagnostic messages. */
    protected final ValueChecker checker;

    /** The ClassLoader.getResourceAsStream(String) method. */
    protected final ExecutableElement getResourceAsStream;

    /** The Properties.getProperty(String) method. */
    protected final ExecutableElement getProperty;

    /** The Properties.getProperty(String, String) method. */
    protected final ExecutableElement getPropertyWithDefaultValue;

    /** The Properties.load(String) method. */
    protected final ExecutableElement propertiesLoad;

    /**
     * Create a new PropertyFileHandler.
     *
     * @param env the processing environment
     * @param factory the annotated type factory
     * @param checker the checker to use
     */
    public PropertyFileHandler(
            ProcessingEnvironment env, ValueAnnotatedTypeFactory factory, ValueChecker checker) {
        this.env = env;
        this.factory = factory;
        this.checker = checker;

        getResourceAsStream =
                TreeUtils.getMethod(
                        java.lang.ClassLoader.class.getName(), "getResourceAsStream", 1, env);
        getProperty =
                TreeUtils.getMethod(java.util.Properties.class.getName(), "getProperty", 1, env);
        getPropertyWithDefaultValue =
                TreeUtils.getMethod(java.util.Properties.class.getName(), "getProperty", 2, env);
        propertiesLoad =
                TreeUtils.getMethod(
                        java.util.Properties.class.getName(), "load", env, "java.io.InputStream");
    }

    /**
     * Return true if the target annotation is in the property file qualifier hierarchy.
     *
     * @param anno the annotation to check
     * @return true if the target annotation is in the property file qualifier hierarchy.
     */
    public static boolean inPropertyFileQualifierHierarchy(AnnotationMirror anno) {
        return AnnotationUtils.areSameByClass(anno, PropertyFile.class)
                || AnnotationUtils.areSameByClass(anno, PropertyFileUnknown.class)
                || AnnotationUtils.areSameByClass(anno, PropertyFileBottom.class);
    }

    /**
     * Handle the property file. Refine the return type of {@link
     * java.lang.ClassLoader#getResourceAsStream(String)}, {@link
     * java.util.Properties#getProperty(String)}, and {@link
     * java.util.Properties#getProperty(String, String)}:
     *
     * <ul>
     *   <li>Refine the return type of {@code getResourceAsStream(propFile)} to
     *       {@literal @}PropertyFile(propFile),
     *   <li>refine the return type of {@code property.getProperty("URL")} from
     *       {@literal @}UnknownVal to {@literal @}StringVal("the value of key 'URL' in the property
     *       file") if {@code property} has type {@literal @}PropertyFile(), and
     *   <li>refine the return type of {@code property.getProperty("URL", "default value")} to
     *       {@literal @}StringVal() if {@code property} has type {@literal @}PropertyFile().
     *       Default value will be applied when the target key does not exist.
     * </ul>
     *
     * <p>Used in {@link
     * org.checkerframework.common.value.ValueTreeAnnotator#visitMethodInvocation(MethodInvocationTree,
     * AnnotatedTypeMirror)}.
     *
     * @param node the method invocation tree
     * @param annotatedTypeMirror the annotated type mirror
     */
    public void handle(MethodInvocationTree node, AnnotatedTypeMirror annotatedTypeMirror) {
        if (TreeUtils.isMethodInvocation(node, getResourceAsStream, env)) {
            AnnotationMirror stringVal = getStringValFromArgument(node, 0);
            if (stringVal == null) {
                return;
            }
            String propFile = getValueFromStringVal(stringVal);
            if (propFile == null) {
                return;
            }
            annotatedTypeMirror.replaceAnnotation(createPropertyFileAnnotation(propFile));
        } else if (TreeUtils.isMethodInvocation(node, getProperty, env)) {
            AnnotationMirror propertyFile =
                    factory.getReceiverType(node).getAnnotation(PropertyFile.class);
            AnnotationMirror stringAnnotation = getStringValFromArgument(node, 0);
            if (propertyFile == null || stringAnnotation == null) {
                return;
            }
            String propFile = getValueFromPropFileAnnotation(propertyFile);
            if (propFile == null) {
                return;
            }
            String propKey = getValueFromStringVal(stringAnnotation);
            Pair<String, String> propValues =
                    readValueFromPropertyFile(propFile, propKey, null, node);
            if (propValues == null) {
                return;
            }
            annotatedTypeMirror.replaceAnnotation(createStringAnnotation(propValues.first));
        } else if (TreeUtils.isMethodInvocation(node, getPropertyWithDefaultValue, env)) {
            AnnotationMirror propFileAnnotation =
                    factory.getReceiverType(node).getAnnotation(PropertyFile.class);
            AnnotationMirror stringAnnotationArg0 = getStringValFromArgument(node, 0);
            AnnotationMirror stringAnnotationArg1 = getStringValFromArgument(node, 1);
            if (propFileAnnotation == null || stringAnnotationArg0 == null) {
                return;
            }
            String propFile = getValueFromPropFileAnnotation(propFileAnnotation);
            if (propFile == null) {
                return;
            }
            String propKey = getValueFromStringVal(stringAnnotationArg0);
            String defaultValue;
            if (stringAnnotationArg1 != null) {
                defaultValue = getValueFromStringVal(stringAnnotationArg1);
            } else {
                defaultValue = null;
            }
            Pair<String, String> propValues =
                    readValueFromPropertyFile(propFile, propKey, defaultValue, node);
            if (propValues == null) {
                return;
            }
            if (propValues.first.equals(propValues.second)) {
                annotatedTypeMirror.replaceAnnotation(createStringAnnotation(propValues.first));
            } else {
                annotatedTypeMirror.replaceAnnotation(
                        createStringAnnotation(propValues.first, propValues.second));
            }
        }
    }

    /**
     * Handle the property file. Refine the receiver type of {@link
     * java.util.Properties#load(InputStream)}. Used in {@link
     * ValueTransfer#visitMethodInvocation(MethodInvocationNode, TransferInput)}. When {@code
     * property.load(inputStream)} is called, try to propagate {@code inputstream}'s type to {@code
     * property}.
     *
     * @param node the method invocation tree
     * @param result the transfer result
     */
    public void handle(MethodInvocationNode node, TransferResult<CFValue, CFStore> result) {
        MethodInvocationTree methodInvocationTree = node.getTree();
        if (TreeUtils.isMethodInvocation(methodInvocationTree, propertiesLoad, env)) {
            ExpressionTree arg0 = methodInvocationTree.getArguments().get(0);
            AnnotationMirror propFileAnnotation =
                    factory.getAnnotatedType(arg0).getAnnotation(PropertyFile.class);
            if (propFileAnnotation == null) {
                return;
            }
            String propFile = getValueFromPropFileAnnotation(propFileAnnotation);
            if (propFile == null) {
                return;
            }
            Node receiver = node.getTarget().getReceiver();
            Receiver receiverRec = FlowExpressions.internalReprOf(factory, receiver);
            propFileAnnotation = createPropertyFileAnnotation(propFile);
            if (result.containsTwoStores()) {
                CFStore thenStore = result.getThenStore();
                CFStore elseStore = result.getElseStore();
                thenStore.insertValue(receiverRec, propFileAnnotation);
                elseStore.insertValue(receiverRec, propFileAnnotation);
            } else {
                CFStore regularStore = result.getRegularStore();
                regularStore.insertValue(receiverRec, propFileAnnotation);
            }
        }
    }

    /**
     * Return true if two {@literal @}PropertyFile annotations' element values are the same, else
     * false.
     *
     * @param subAnno the sub annotation
     * @param superAnno the super annotation
     * @return true if the two annotations' values are the same
     */
    protected boolean propFileElementValueEquals(
            AnnotationMirror subAnno, AnnotationMirror superAnno) {
        String subAnnoElementValue =
                AnnotationUtils.getElementValue(subAnno, "value", String.class, false);
        String superAnnoElementValue =
                AnnotationUtils.getElementValue(superAnno, "value", String.class, false);
        return superAnnoElementValue.equals(subAnnoElementValue);
    }

    /**
     * Tests whether subAnno is subtype of superAnno in the property file type hierarchy.
     *
     * @param subAnno the sub qualifier
     * @param superAnno the super qualifier
     * @return true if subAnno is subtype of superAnno
     */
    protected boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
        if (AnnotationUtils.areSameByClass(subAnno, PropertyFileBottom.class)
                || AnnotationUtils.areSameByClass(superAnno, PropertyFileUnknown.class)) {
            return true;
        } else if (AnnotationUtils.areSameByClass(subAnno, PropertyFileUnknown.class)
                || AnnotationUtils.areSameByClass(superAnno, PropertyFileBottom.class)) {
            return false;
        } else if (AnnotationUtils.areSameByClass(subAnno, PropertyFile.class)
                && AnnotationUtils.areSameByClass(superAnno, PropertyFile.class)) {
            return propFileElementValueEquals(subAnno, superAnno);
        } else {
            throw new BugInCF("We should never reach here.");
        }
    }

    /**
     * Try to read the value of the key in the provided property file.
     *
     * @param propFile the property file to open
     * @param key the key to find in the property file
     * @param defaultValue the default value of that key
     * @param node the method invocation tree used for reporting warning
     * @return the value of the key in the property file
     */
    protected Pair<String, String> readValueFromPropertyFile(
            String propFile, String key, String defaultValue, MethodInvocationTree node) {
        String res = null;
        try {
            Properties prop = new Properties();
            ClassLoader cl = this.getClass().getClassLoader();
            if (cl == null) {
                // the class loader is null if the system class loader was
                // used
                cl = ClassLoader.getSystemClassLoader();
            }
            InputStream in = cl.getResourceAsStream(propFile);
            if (in == null) {
                // if the classloader didn't manage to load the file, try
                // whether a FileInputStream works. For absolute paths this
                // might help.
                try {
                    in = new FileInputStream(propFile);
                } catch (FileNotFoundException e) {
                    // ignore
                }
            }
            if (in == null) {
                checker.message(Kind.WARNING, "Couldn't find the properties file: " + propFile);
                return null;
            }
            prop.load(in);
            Set<String> keyNames = prop.stringPropertyNames();
            if (!keyNames.contains(key)) {
                checker.reportWarning(node, "key.not.exist.in.properties.file", key, propFile);
                if (defaultValue == null) {
                    return null;
                }
            }
            if (defaultValue == null) {
                res = prop.getProperty(key);
            } else {
                res = prop.getProperty(key, defaultValue);
            }
        } catch (Exception e) {
            checker.message(
                    Kind.WARNING, "Exception in PropertyFileHandler.readPropertyFromFile: " + e);
        }
        return Pair.of(res, defaultValue);
    }

    /**
     * Create a {@literal @}PropertyFile(value) annotation.
     *
     * @param value the value to set in the created annotation
     * @return the created annotation
     */
    protected AnnotationMirror createPropertyFileAnnotation(String value) {
        AnnotationBuilder builder = new AnnotationBuilder(env, PropertyFile.class);
        builder.setValue("value", value);
        return builder.build();
    }

    /**
     * Create a {@literal @}StringVal(values) annotation.
     *
     * @param values the values to set in the created annotation
     * @return the created annotation
     */
    protected AnnotationMirror createStringAnnotation(String... values) {
        return factory.createStringAnnotation(Arrays.asList(values));
    }

    /**
     * Get the value of the {@literal @}StringVal() annotation. Return null when there are more than
     * one values (otherwise the PropertyFileHandler may makes the Value Checker unsound).
     *
     * @param stringVal the {@literal @}StringVal() annotation
     * @return the first value of the {@literal @}StringVal() annotation
     */
    protected String getValueFromStringVal(AnnotationMirror stringVal) {
        List<String> values = getStringValues(stringVal);
        if (values.size() == 1) {
            return values.get(0);
        } else {
            return null;
        }
    }

    /**
     * Get the value of the {@literal @}PropertyFile() annotation.
     *
     * @param propertyFile the {@literal @}PropertyFile() annotation
     * @return the value of the {@literal @}PropertyFile() annotation
     */
    protected static String getValueFromPropFileAnnotation(AnnotationMirror propertyFile) {
        return AnnotationUtils.getElementValue(propertyFile, "value", String.class, false);
    }

    /**
     * Get the {@literal @}StringVal() annotation from the argument of the method invocation.
     *
     * @param node the method invocation tree
     * @param position the position of the argument
     * @return the target {@literal @}StringVal() annotation
     */
    protected AnnotationMirror getStringValFromArgument(MethodInvocationTree node, int position) {
        ExpressionTree arg = node.getArguments().get(position);
        return factory.getAnnotatedType(arg).getAnnotation(StringVal.class);
    }
}
