#!/bin/bash

# A script to run a single JHipster integration test by its class name.
# It finds the test file, handles ambiguity, and constructs the correct Maven command.

# --- Configuration ---
TEST_SOURCE_PREFIX="src/test/java/"

# --- Helper Functions ---
print_usage() {
    echo "Purpose: Runs a JHipster/Maven integration test by its class name."
    echo ""
    echo "Usage:   $0 ClassName"
    echo "         $0 ClassName MethodName"
    echo "         $0 ClassName.MethodName"
    echo ""
    echo "Examples:"
    echo "  # Run all tests in a class"
    echo "  $0 PaymentTransactionResourceIT"
    echo ""
    echo "  # Run a single method (two arguments)"
    echo "  $0 PaymentTransactionResourceIT checkCurrencyIsRequired"
    echo ""
    echo "  # Run a single method (dot notation)"
    echo "  $0 PaymentTransactionResourceIT.checkCurrencyIsRequired"
    exit 1
}

# --- Script Logic ---

# 1. PARSE ARGUMENTS to determine Class Name and Method Name
# -----------------------------------------------------------
if [ "$#" -eq 0 ] || [ "$#" -gt 2 ]; then
    print_usage
fi

CLASS_NAME=""
TEST_METHOD=""

# Handle "ClassName.MethodName" format
if [[ "$#" -eq 1 && "$1" == *.* ]]; then
    CLASS_NAME="${1%%.*}"
    TEST_METHOD="${1#*.}"
# Handle "ClassName MethodName" format
elif [ "$#" -eq 2 ]; then
    CLASS_NAME=$1
    TEST_METHOD=$2
# Handle "ClassName" format
else
    CLASS_NAME=$1
fi

echo "--> Test Class:  '${CLASS_NAME}'"
if [ -n "$TEST_METHOD" ]; then
    echo "--> Test Method: '${TEST_METHOD}'"
fi
echo

# 2. FIND THE TEST FILE corresponding to the Class Name
# -----------------------------------------------------------
FILE_TO_FIND="${CLASS_NAME}.java"
echo "--> Searching for test file: ${FILE_TO_FIND}"

# Use find to locate the file, searching only in the current directory downwards.
# The search results are stored in a variable.
SEARCH_RESULTS=$(find . -type f -name "${FILE_TO_FIND}")
# Count the number of lines in the result.
NUM_RESULTS=$(echo "${SEARCH_RESULTS}" | grep -c .)

if [ "${NUM_RESULTS}" -eq 0 ]; then
    echo "    Error: Could not find any test file named '${FILE_TO_FIND}'."
    exit 1
fi

if [ "${NUM_RESULTS}" -gt 1 ]; then
    echo "    Error: Found multiple files named '${FILE_TO_FIND}'. Please be more specific."
    echo "${SEARCH_RESULTS}"
    exit 1
fi

TEST_FILE_PATH=${SEARCH_RESULTS}
echo "    Found: ${TEST_FILE_PATH}"
echo

# 3. CONSTRUCT THE MAVEN COMMAND
# -----------------------------------------------------------
echo "--> Building Maven command..."
# Convert file path (e.g., ./src/test/java/com/app/MyIT.java) to FQCN (e.g., com.app.MyIT)
FQCN=$(echo "$TEST_FILE_PATH" | sed -e "s#^./${TEST_SOURCE_PREFIX}##" -e 's#\.java$##' -e 's#/#.#g')
echo "    Fully Qualified Class Name: ${FQCN}"

TEST_TARGET="${FQCN}"
if [ -n "$TEST_METHOD" ]; then
    TEST_TARGET="${FQCN}#${TEST_METHOD}"
fi
echo "    Maven Test Target (-Dit.test): ${TEST_TARGET}"

MAVEN_COMMAND="./mvnw -Pprod,testcontainers verify -Dfail-if-no-tests=false -Dit.test=${TEST_TARGET}"

# 4. EXECUTE
# -----------------------------------------------------------
echo
echo "------------------------------------------------------------------------"
echo "Executing command:"
echo "$MAVEN_COMMAND"
echo "------------------------------------------------------------------------"
echo

${MAVEN_COMMAND}
