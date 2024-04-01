#!/bin/bash

set -e
set -o verbose
set -o xtrace
export SHELLOPTS
echo "SHELLOPTS=${SHELLOPTS}"

# Optional argument $1 is the group.
GROUPARG=$1
echo "GROUPARG=$GROUPARG"
# These are all the Java projects at https://github.com/eisop-plume-lib as of Dec 2022.
if [[ "${GROUPARG}" == "bcel-util" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "bibtex-clean" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "html-pretty-print" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "icalavailable" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "javadoc-lookup" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "lookup" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "multi-version-control" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "options" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "plume-util" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "reflection-util" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "require-javadoc" ]]; then PACKAGES=("${GROUPARG}"); fi
if [[ "${GROUPARG}" == "all" ]] || [[ "${GROUPARG}" == "" ]]; then
    if java -version 2>&1 | grep version | grep 1.8 ; then
        # options master branch does not compile under JDK 8
        PACKAGES=(bcel-util bibtex-clean html-pretty-print icalavailable javadoc-lookup lookup multi-version-control plume-util reflection-util require-javadoc)
    else
        PACKAGES=(bcel-util bibtex-clean html-pretty-print icalavailable javadoc-lookup lookup multi-version-control options plume-util reflection-util require-javadoc)
    fi
fi
if [ -z ${PACKAGES+x} ]; then
  echo "Bad group argument '${GROUPARG}'"
  exit 1
fi
echo "PACKAGES=" "${PACKAGES[@]}"


SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
# shellcheck disable=SC1090 # In newer shellcheck than 0.6.0, pass: "-P SCRIPTDIR" (literally)
export ORG_GRADLE_PROJECT_useJdk17Compiler=true
source "$SCRIPTDIR"/build.sh


failing_packages=""
echo "PACKAGES=" "${PACKAGES[@]}"
for PACKAGE in "${PACKAGES[@]}"; do
  echo "PACKAGE=${PACKAGE}"
  PACKAGEDIR="/tmp/${PACKAGE}"
  rm -rf "${PACKAGEDIR}"
  "$SCRIPTDIR/.plume-scripts/git-clone-related" eisop-plume-lib "${PACKAGE}" "${PACKAGEDIR}" -q --single-branch --depth 250
  if [ "${PACKAGE}" = "options" ]; then
    (cd "${PACKAGEDIR}" && git checkout a91f0e15db05b19a150a76eccb7309a47fde2931 && cd ..)
  fi
  if [ "${PACKAGE}" = "plume-util" ]; then
    (cd "${PACKAGEDIR}" && git checkout 5a8399cd5a38408c129ebc8ee8c198b439634fd0 && cd ..)
  fi
  if [ "${PACKAGE}" = "bcel-util" ]; then
    (cd "${PACKAGEDIR}" && git checkout be503985957cc29d9b371b3241761f21bd261cb0 && cd ..)
  fi
  if [ "${PACKAGE}" = "bibtex-clean" ]; then
    (cd "${PACKAGEDIR}" && git checkout 79248b39a13adcd46268f056df6502b285a8e0b0 && cd ..)
  fi
  if [ "${PACKAGE}" = "html-pretty-print" ]; then
    (cd "${PACKAGEDIR}" && git checkout 3cb838f7fa30e6de2a9c6a9b102bb9f517d2dd15 && cd ..)
  fi
  if [ "${PACKAGE}" = "icalavailable" ]; then
    (cd "${PACKAGEDIR}" && git checkout d7857ae0aa0cedccac2c623fff8525ba3978006a && cd ..)
  fi
  if [ "${PACKAGE}" = "javadoc-lookup" ]; then
    (cd "${PACKAGEDIR}" && git checkout 83e5bfabd3fdaa5d725520b1aa33b25bb6797d37 && cd ..)
  fi
  if [ "${PACKAGE}" = "lookup" ]; then
    (cd "${PACKAGEDIR}" && git checkout 6a138a1abde5b20eaefcefab2f815463ddd5013c && cd ..)
  fi
  if [ "${PACKAGE}" = "multi-version-control" ]; then
    (cd "${PACKAGEDIR}" && git checkout e75c9e0c74823a528239525e280e097490a643a9 && cd ..)
  fi
  if [ "${PACKAGE}" = "require-javadoc" ]; then
    (cd "${PACKAGEDIR}" && git checkout 6eea833f403509bad8923430a29843d272e524cc && cd ..)
  fi
  if [ "${PACKAGE}" = "reflection-util" ]; then
    (cd "${PACKAGEDIR}" && git checkout cd00c7659a6508436b978955b08896bb7a461e61 && cd ..)
  fi
  # Uses "compileJava" target instead of "assemble" to avoid the javadoc error "Error fetching URL:
  # https://docs.oracle.com/en/java/javase/17/docs/api/" due to network problems.
  echo "About to call ./gradlew --console=plain -PcfLocal compileJava"
  # Try twice in case of network lossage.
  (cd "${PACKAGEDIR}" && (./gradlew --console=plain -PcfLocal compileJava || (sleep 60 && ./gradlew --console=plain -PcfLocal compileJava))) || failing_packages="${failing_packages} ${PACKAGE}"
done

if [ -n "${failing_packages}" ] ; then
  echo "Failing packages: ${failing_packages}"
  exit 1
fi
