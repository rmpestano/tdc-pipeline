#!/usr/bin/env groovy

/**
 * Send notifications based on build status string
 */
def call(String buildStatus) {
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESSFUL'

  def color = buildStatus == 'SUCCESSFUL' ? 'good' : 'danger'

  def message = "${currentBuild.fullDisplayName} *${buildStatus}*. (<${env.BUILD_URL}|Open>)"


  slackSend (channel: '#builds', color: color, message: message)


}