package com.binxhealth.hermes.message

import com.binxhealth.hermes.utils.HttpStatusUtils
import grails.gorm.transactions.Transactional

/**
 * This service handles any and all changes to FailedMessage data, including creation and deletion of specific messages.
 *
 * @author Maura Warner
 */
@Transactional
class FailedMessageManagerService {

    FailedMessage createFailedMessage(MessageCommand messageData, int statusCode) {
        FailedMessage message = new FailedMessage()
        message.messageData = messageData.toMap()
        message.statusCode = statusCode
        message.save(failOnError: true)
    }

    void purgeMessage(FailedMessage message) {
        message.delete(failOnError: true)
    }

    void purgeMessages(Set<FailedMessage> messages) {
        messages*.delete(failOnError: true)
    }

    void completeFailedRetryProcess(FailedMessage message, int finalStatusCode) {
        message.statusCode = finalStatusCode
        message.save(failOnError: true)
    }

    /**
     * Finds all FailedMessages currently eligible for retry.  Messages that failed with 3xx or 4xx error codes
     * are ineligible for retry as they are invalid; only messages that failed with 5xx error codes or ConnectExceptions
     * should be retried.
     * @return FailedMessages to retry
     */
    @Transactional(readOnly = true)
    Set<FailedMessage> gatherFailedMessagesForRetry() {
        Set<FailedMessage> messages = FailedMessage.createCriteria().listDistinct {
            or {
                eq('statusCode', HttpStatusUtils.CONNECTION_FAILURE_CODE)
                ge('statusCode', 500)
            }
        } as Set<FailedMessage>
        return messages
    }

}
