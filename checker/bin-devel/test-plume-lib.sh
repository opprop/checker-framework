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
    (cd "${PACKAGEDIR}" && git checkout a60588b489fe804b6b2803443c26a3f95bcf3c36 && cd ..)
  fi
  if [ "${PACKAGE}" = "plume-util" ]; then
    (cd "${PACKAGEDIR}" && git checkout e6cfe66414b27e82afba84dff907169286e57dba && cd ..)
  fi
  if [ "${PACKAGE}" = "bcel-util" ]; then
    (cd "${PACKAGEDIR}" && git checkout 15a290d99744bde688864349146ddfc7e0f36056 && cd ..)
  fi
  if [ "${PACKAGE}" = "bibtex-clean" ]; then
    (cd "${PACKAGEDIR}" && git checkout d1f936b436a885248bc46dbe8b0fb5408a292a1d && cd ..)
  fi
  if [ "${PACKAGE}" = "html-pretty-print" ]; then
    (cd "${PACKAGEDIR}" && git checkout 7b787c09bb256a9767ab4b61532b7041452c7817 && cd ..)
  fi
  if [ "${PACKAGE}" = "icalavailable" ]; then
    (cd "${PACKAGEDIR}" && git checkout 43a753ebd27245d4fc8ab59918c58461f55ef644 && cd ..)
  fi
  if [ "${PACKAGE}" = "javadoc-lookup" ]; then
    (cd "${PACKAGEDIR}" && git checkout 799fbafb2aba8ac0efba987a447bdf949e31b669 && cd ..)
  fi
  if [ "${PACKAGE}" = "lookup" ]; then
    (cd "${PACKAGEDIR}" && git checkout 075d9fff7be57b3c540b979f35187db1ea8b18eb && cd ..)
  fi
  if [ "${PACKAGE}" = "multi-version-control" ]; then
    (cd "${PACKAGEDIR}" && git checkout ca0f076eb7f56d7d1b1fbb2b78a4a54feb298b39 && cd ..)
  fi
  if [ "${PACKAGE}" = "require-javadoc" ]; then
    (cd "${PACKAGEDIR}" && git checkout e8181d045b07b1728b3598b2c7646246acb701c3 && cd ..)
  fi
  if [ "${PACKAGE}" = "reflection-util" ]; then
    (cd "${PACKAGEDIR}" && git checkout 4dcec57d1bdc9d827977602cdc2b623aa533a26c && cd ..)
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
