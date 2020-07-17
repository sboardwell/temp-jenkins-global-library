pipeline {
  agent none

  environment {
    CREDS_FILE         = "aws-credentials-jenkins"
    DOCKER_TAG         = "8c1518e8354eecb93d5ffa4725567911a1cc713c"
    BYOC_IMAGE         = "498979307932.dkr.ecr.us-east-2.amazonaws.com/byoc"
    CURRENT_BYOC_IMAGE = "${BYOC_IMAGE}:${DOCKER_TAG}"
  }

  options {
    ansiColor('gnome-terminal')
  }

  parameters {
    string(name: 'CHAT_COMMAND', defaultValue: '', description: 'Enter a chat comment to run a specific command.', trim: true)
  }

  stages {
    stage('Init') {
      steps {
        initChatOpsEnv()
      }
    }

    stage('Image Not Found') {
      agent {
        label "jnlp"
      }
      when {
        anyOf {
          branch 'master'

          expression {
            // If the docker tag already exists, we can safely skip this run since it already exists and will not differ
            return !sh(returnStdout: true, script: "docker images -q ${CURRENT_BYOC_IMAGE}")
          }
        }
      }
      stages {
        stage('Echo') {
          steps {
            script {
              sh "echo hello from $STAGE_NAME"
            }
          }
        }
      }
    }
    stage('Image Found') {
      agent {
        label "jnlp"
      }
      when {
        anyOf {
          branch 'master'

          expression {
            // If the docker tag already exists, we can safely skip this run since it already exists and will not differ
            return sh(returnStdout: true, script: "docker images -q ${CURRENT_BYOC_IMAGE}")
          }
        }
      }
      stages {
        stage('Echo') {
          steps {
            script {
              sh "echo hello from $STAGE_NAME"
            }
          }
        }
      }
    }
    stage('Image Check No Agent') {
      when {
        anyOf {
          branch 'master'

          expression {
            // If the docker tag already exists, we can safely skip this run since it already exists and will not differ
            return sh(returnStdout: true, script: "docker images -q ${CURRENT_BYOC_IMAGE}")
          }
        }
      }
      stages {
        stage('Echo') {
          steps {
            script {
              sh "echo hello from $STAGE_NAME"
            }
          }
        }
      }
    }
  }
}

// Parses the job trigger
def parseJobTrigger() {
  if (!params.CHAT_COMMAND.isEmpty()) {
    env.CHATOPS_COMMENT = params.CHAT_COMMAND.trim()
    env.CHATOPS_USER = currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause).getUserName()
    return;
  }

  def triggerCause = currentBuild.rawBuild.getCause(org.jenkinsci.plugins.pipeline.github.trigger.IssueCommentCause)
  if (!triggerCause) return;

  env.CHATOPS_COMMENT = triggerCause.comment.trim()
  env.CHATOPS_USER = triggerCause.userLogin
}

// Determining the available comment causing the update is manaul until a new release is available including
// the env vars: https://github.com/jenkinsci/pipeline-github-plugin#environment-variables.
//
// See https://github.com/jenkinsci/pipeline-github-plugin/issues/83.
def initChatOpsEnv() {
  parseJobTrigger()

  if (!env.CHATOPS_COMMENT) return; // Note: this was not triggered by an issue comment or Jenkins build param

  // This pattern will find two named groups: command, args.
  def pattern = "/(?<command>packer|cft|cloudformation|cdk) (?<args>[A-Za-z0-9 =._-]*)"

  // Use the pattern to get a Matcher instance
  def matcher = (env.CHATOPS_COMMENT =~ pattern)

  // Set a boolean to make it easy to determine if the build was triggered via ChatOps
  env.CHATOPS_TRIGGER = matcher.matches()

  // Stop processing if there are no matches
  if (!env.CHATOPS_TRIGGER) return;

  // Convert the Matcher to an Array
  def matches = matcher.findAll()[0]

  // Set the command to the first group from the trigger
  env.CHATOPS_COMMAND = matches[1]

  // Set the latter part of the comment to the args
  def args = matches[2]

  def argsSplit = args.split(" ")

  // Set the subcommand to the first group of text after the cli name
  env.CHATOPS_SUBCOMMAND = argsSplit[0]

  def subcommandSplit = env.CHATOPS_SUBCOMMAND.split('\\.')

  if (subcommandSplit.length > 1) {
    // Set the target to the first part of the command; ex - `byoc.build` means the target is `byoc`
    env.CHATOPS_SUBCOMMAND_TARGET = subcommandSplit[0]

    // Set the target action to the second part of the command; ex - `byoc.build` means the target action is `build`
    env.CHATOPS_SUBCOMMAND_TARGET_ACTION = subcommandSplit[1]
  }
}
