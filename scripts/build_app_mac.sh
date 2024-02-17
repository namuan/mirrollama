#!/bin/bash

# ------ ENVIRONMENT --------------------------------------------------------
# The script depends on various environment variables to exist in order to
# run properly. The java version we want to use, the location of the java
# binaries (java home), and the project version as defined inside the pom.xml
# file, e.g. 1.0-SNAPSHOT.
#
# PROJECT_VERSION: version used in pom.xml, e.g. 1.0-SNAPSHOT
# APP_VERSION: the application version, e.g. 1.0.0, shown in "about" dialog

MAIN_JAR="gpt-fx-$PROJECT_VERSION.jar"
APPLICATION_NAME=GptFx
PACKAGE_IDENTIFIER=com.github.namuan.gptfx
MAIN_CLASS=${PACKAGE_IDENTIFIER}.MainAppKt
VENDOR="DeskRiders"
COPYRIGHT_MESSAGE="Copyright © 2022-23 DeskRiders"

# Set desired installer type: "app-image", "dmg", "pkg".
INSTALLER_TYPE=app-image

echo "java home: $JAVA_HOME"
echo "project version: $PROJECT_VERSION"
echo "app version: $APP_VERSION"
echo "main JAR file: $MAIN_JAR"

# ------ SETUP DIRECTORIES AND FILES ----------------------------------------
mkdir -vp target/installer/input/libs/

cp -R target/app/lib/* target/installer/input/libs/
cp target/"${MAIN_JAR}" target/installer/input/libs/

# ------ PACKAGING ----------------------------------------------------------
echo "Creating installer of type $INSTALLER_TYPE"

"$JAVA_HOME"/bin/jpackage \
  --type ${INSTALLER_TYPE} \
  --dest target/installer \
  --input target/installer/input/libs \
  --name ${APPLICATION_NAME} \
  --main-class ${MAIN_CLASS} \
  --main-jar "${MAIN_JAR}" \
  --java-options -Xmx2048m \
  --runtime-image target/app \
  --icon src/main/resources/icons/app.icns \
  --app-version 1.0.0 \
  --vendor ${VENDOR} \
  --copyright "${COPYRIGHT_MESSAGE}" \
  --mac-package-identifier ${PACKAGE_IDENTIFIER} \
  --mac-package-name ${APPLICATION_NAME}
