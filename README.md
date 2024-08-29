
# Integración Continua con Pruebas Unitarias de Servicios Web utilizando SoapUI

Este proyecto demuestra cómo implementar pruebas unitarias de servicios web en un ciclo de integración continua utilizando **SoapUI** y **Jenkins**.

## Índice
1. [Descripción General](#descripción-general)
2. [Repositorio del Proyecto](#repositorio-del-proyecto)
3. [Pipeline de Jenkins](#pipeline-de-jenkins)
   - [Código del Pipeline](#código-del-pipeline)
4. [Resultados de la Ejecución](#resultados-de-la-ejecución)
5. [Notas Adicionales](#notas-adicionales)

## Descripción General

El objetivo de este proyecto es asegurar la calidad del servicio web antes de su implementación en producción. Para ello, se han configurado pruebas unitarias que se ejecutan automáticamente en cada cambio del código.

## Repositorio del Proyecto

El código fuente y la configuración de las pruebas están alojados en el siguiente repositorio de GitHub:

- [Maven-SoapUi](https://github.com/vngerus/Maven-SoapUi)

## Pipeline de Jenkins

El pipeline de Jenkins está configurado para realizar las siguientes tareas:

1. **Verificación de Configuración**: Verifica que las herramientas necesarias como Java, Maven y SoapUI estén correctamente configuradas.
2. **Clonar el Código**: Clona el repositorio de GitHub con el código fuente del proyecto.
3. **Compilar el Proyecto**: Utiliza Maven para compilar el proyecto y generar los archivos necesarios.
4. **Verificar Archivo SoapUI**: Verifica la existencia del archivo `SoapService.xml` que contiene la definición del proyecto de pruebas en SoapUI.
5. **Ejecutar Pruebas con SoapUI**: Ejecuta las pruebas unitarias definidas en el archivo `SoapService.xml` utilizando SoapUI.
6. **Archivar Resultados**: Guarda los resultados de las pruebas para su posterior revisión.
7. **Enviar Notificaciones a Slack**: Envía notificaciones a un canal de Slack con el estado del pipeline.
8. **Limpieza del Workspace**: Limpia el espacio de trabajo al final del proceso.

### Código del Pipeline

El siguiente es el código del pipeline implementado en Jenkins:

```groovy
pipeline {
    agent any

    environment {
        JMETER_HOME = 'D:\Archivos\apache-jmeter-5.6.3\bin'
        JMETER_TEST = 'D:\Archivos\Respuesta.jmx'
        JMETER_RESULTS = 'D:\Archivos\resultados.jtl'
        JMETER_REPORT_DIR = 'D:\Archivos\jmeter-report'
    }

    stages {
        stage('Run JMeter Tests') {
            steps {
                bat """
                ${JMETER_HOME}\jmeter.bat -n -t ${JMETER_TEST} -l ${JMETER_RESULTS} -e -o ${JMETER_REPORT_DIR}
                """
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

## Resultados de la Ejecución

- **Compilación Exitosa**: El proyecto fue compilado correctamente utilizando Maven.
- **Ejecución de Pruebas**: Las pruebas SoapUI se ejecutaron con éxito. El archivo `SoapService.xml` fue verificado y utilizado para ejecutar los casos de prueba.
- **Notificaciones**: Se enviaron notificaciones a Slack en cada etapa relevante del pipeline.
- **Limpieza**: El workspace fue limpiado después de la ejecución del pipeline.

## Notas Adicionales

- Asegúrate de que todas las rutas y configuraciones en el pipeline estén adaptadas a tu entorno de desarrollo.
- Revisa regularmente los resultados de las pruebas archivadas para detectar cualquier posible fallo en los servicios web.
