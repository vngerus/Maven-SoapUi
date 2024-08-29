
# Implementa pruebas unitarias de servicios web al ciclo de integración continua utilizando soapUI.

## Índice
1. [Introducción](#introducción)
    - [Objetivos](#objetivos)
2. [Configuración del Entorno](#configuración-del-entorno)
    - [Requisitos](#requisitos)
    - [Variables de Entorno](#variables-de-entorno)
    - [Instalación](#instalación)
3. [Estructura del Proyecto](#estructura-del-proyecto)
    - [Arquitectura](#arquitectura)
    - [Estructura de Carpetas](#estructura-de-carpetas)
4. [Pipeline de Jenkins](#pipeline-de-jenkins)
    - [Descripción del Pipeline](#descripción-del-pipeline)
    - [Estructura del Pipeline](#estructura-del-pipeline)
    - [Configuración del Pipeline en Jenkins](#configuración-del-pipeline-en-jenkins)
    - [Resultados y Logs](#resultados-y-logs)
5. [Uso y Mantenimiento](#uso-y-mantenimiento)
    - [Actualización de Dependencias](#actualización-de-dependencias)
    - [Solución de Problemas](#solución-de-problemas)
6. [Conclusiones y Próximos Pasos](#conclusiones-y-próximos-pasos)
7. [Referencias](#referencias)
8. [Anexos](#anexos)
    - [Archivos de Configuración](#archivos-de-configuración)
    - [Scripts Utilizados](#scripts-utilizados)

---

## Introducción

Este proyecto utiliza **SoapUI** y **Jenkins** para implementar pruebas unitarias de servicios web en un ciclo de integración continua. Las pruebas están diseñadas para garantizar la calidad del servicio web antes de su implementación en producción.

**Repositorio:** [Maven-SoapUI](https://github.com/vngerus/Maven-SoapUi)

### Objetivos
- Implementar pruebas unitarias de servicios web utilizando SoapUI.
- Integrar estas pruebas en un ciclo de integración continua mediante Jenkins.
- Asegurar que los servicios web funcionen correctamente antes de su despliegue en producción.

## Configuración del Entorno

### Requisitos
- **Java JDK:** Versión 11 o superior.
- **Maven:** Versión 3.6.0 o superior.
- **SoapUI:** Versión 5.7.2 o superior.
- **Jenkins:** Instalado y configurado para ejecutar pipelines.

### Variables de Entorno
- `JAVA_HOME`: Ruta al JDK de Java.
- `MAVEN_HOME`: Ruta de Maven.
- `SOAPUI_HOME`: Ruta a la instalación de SoapUI.

### Instalación
1. Clona el repositorio:
   ```bash
   git clone https://github.com/vngerus/Maven-SoapUi.git
   ```
2. Navega al directorio del proyecto:
   ```bash
   cd Maven-SoapUi
   ```
3. Instala las dependencias utilizando Maven:
   ```bash
   mvn clean install
   ```

## Estructura del Proyecto

### Arquitectura
- **Servicios Web:** Proyectos SOAP que son probados usando SoapUI.
- **Pruebas Automatizadas:** Las pruebas de los servicios web son gestionadas y ejecutadas desde Jenkins.

### Estructura de Carpetas
- `src/main/java`: Código fuente principal del servicio.
- `src/test/java`: Código de pruebas unitarias y de integración.
- `src/test/resources`: Archivos y configuraciones de prueba.

## Pipeline de Jenkins

### Descripción del Pipeline
El pipeline está configurado para automatizar la compilación y prueba de los servicios web, enviar notificaciones a Slack, y limpiar el espacio de trabajo al finalizar.

### Estructura del Pipeline
1. **Verificación de Configuración:**
   - Comprueba que las herramientas necesarias como Java, Maven y SoapUI estén correctamente configuradas.
2. **Clonar el Código:**
   - Clona el repositorio desde GitHub.
3. **Compilación del Proyecto:**
   - Ejecuta `mvn clean install` para compilar el proyecto y generar los archivos necesarios.
4. **Verificación del Archivo SoapUI:**
   - Verifica la existencia del archivo `SoapService.xml`.
5. **Ejecución de Pruebas SoapUI:**
   - Ejecuta las pruebas definidas en SoapUI.
6. **Notificación a Slack:**
   - Envío de notificación al canal `#time-tracker-ci`.
7. **Limpieza del Espacio de Trabajo:**
   - Limpia el espacio de trabajo para evitar acumulaciones.

### Configuración del Pipeline en Jenkins

Ejemplo del archivo `Jenkinsfile`:

```groovy
pipeline {
    agent any

    environment {
        JMETER_HOME = 'D:\Archivos\apache-jmeter-5.6.3\bin'
        JMETER_TEST = 'D:\Archivos\Aserción de Respuesta.jmx'
        JMETER_RESULTS = 'D:\Archivos\resultados.jtl'
        JMETER_REPORT_DIR = 'D:\Archivos\jmeter-report'
    }

    stages {
        stage('Run JMeter Tests') {
            steps {
                bat "${JMETER_HOME}\jmeter.bat -n -t ${JMETER_TEST} -l ${JMETER_RESULTS} -e -o ${JMETER_REPORT_DIR}"
            }
        }
        
        stage('Archive Results') {
            steps {
                archiveArtifacts artifacts: "${JMETER_REPORT_DIR}/**/*", allowEmptyArchive: true
            }
        }
        
        stage('Publish Report') {
            steps {
                publishHTML([allowMissing: false,
                             alwaysLinkToLastBuild: true,
                             keepAll: true,
                             reportDir: "${JMETER_REPORT_DIR}",
                             reportFiles: 'index.html',
                             reportName: 'JMeter Performance Report'])
            }
        }
    }
    
    post {
        always {
            script {
                try {
                    junit '**/TEST-*.xml'
                } catch (Exception e) {
                    echo "No se encontraron resultados JUnit para publicar"
                }
            }
            slackSend(channel: '#time-tracker-ci', message: "Pipeline en proceso: ${currentBuild.fullDisplayName}")
        }
        success {
            slackSend(channel: '#time-tracker-ci', message: "Éxito: El Pipeline se ha completado con éxito: ${currentBuild.fullDisplayName}")
        }
        failure {
            slackSend(channel: '#time-tracker-ci', message: "Fallo: El Pipeline ha fallado: ${currentBuild.fullDisplayName}")
        }
        cleanup {
            cleanWs()
        }
    }
}
```

### Resultados y Logs
- Dependencias descargadas durante la ejecución del pipeline:
  ```bash
  Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-digest/1.0/plexus-digest-1.0.jar (12 kB at 539 kB/s)
  ```
- Ejemplo de resultado de una prueba exitosa:
  ```bash
  [INFO] BUILD SUCCESS
  [INFO] Total time:  03:06 min
  [INFO] Finished at: 2024-08-28T18:16:02-04:00
  ```

## Uso y Mantenimiento

### Actualización de Dependencias
Para actualizar las dependencias, edita el archivo `pom.xml` y ejecuta:
```bash
mvn clean install
```

### Solución de Problemas
- **Errores de Compilación:** Verifica las versiones de Java y Maven.
- **Problemas con SoapUI:** Asegúrate de que `SOAPUI_HOME` esté correctamente configurado.

## Conclusiones y Próximos Pasos

El pipeline automatiza la compilación, pruebas y notificaciones, asegurando un entorno limpio y eficiente para cada ejecución. Los próximos pasos incluyen la integración de más pruebas y la mejora continua del proyecto.

## Referencias
- **SoapUI Documentation:** https://www.soapui.org/docs/
- **Maven Documentation:** https://maven.apache.org/guides/index.html
- **Jenkins Documentation:** https://www.jenkins.io/doc/

## Anexos

### Archivos de Configuración
- `pom.xml`: Archivo de configuración de Maven.
- `Jenkinsfile`: Archivo de configuración del pipeline en Jenkins.

### Scripts Utilizados
Listado y descripción de scripts personalizados, si aplica.
