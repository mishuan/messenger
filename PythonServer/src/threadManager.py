# Python Library Imports
import sys
import os
import time
from threading import Thread, Timer, Lock

# Local Imports
from constants import ThreadManagerConstants, EThreadType, Dictionary
from logger import *

class ThreadManager:
    """
        Thread Manager Class
        Class responsible for launching and tracking threads.
    """

    def __init__(self):
        """
            Constructor
        """
        log(DEBUG, '>>')
        self.iThreadIndex = 0
        self.threadLaunchLock = Lock()
        log(DEBUG, '<<')

    def __del__(self):
        """
            Destructor
        """
        log(DEBUG, '>>')
        self.iThreadIndex = None
        log(DEBUG, '<<')

    def getThreadName(self, eThreadType, iThreadIndex):
        threadName = ""
        if (eThreadType == EThreadType.ThreadedServer): threadName = "[{0}]ThreadedServer".format(iThreadIndex)
        elif (eThreadType == EThreadType.ThreadedServerWorker): threadName = "[{0}]ThreadedServerWorker".format(iThreadIndex)
        elif (eThreadType == EThreadType.Database): threadName = "[{0}]Database".format(iThreadIndex)
        else: threadName = "[{0}]Unknown".format(iThreadIndex)
        return threadName

    def launchNewThread(self, eThreadType, targetFunction, arrInArgs):
        log(DEBUG, '>>')
        thread = None
        try:
            self.threadLaunchLock.acquire(True)
            thread = Thread(target = targetFunction, name = self.getThreadName(eThreadType, self.iThreadIndex), args = arrInArgs)
            thread.start();
        except Exception as e:
            iRetThreadIndex = -1
            log(ERROR, "Unable to launch thread, reason: {0}".format(e))
        finally:
            self.threadLaunchLock.release()

        log(DEBUG, '<< {}'.format(thread))
        return thread

    def launchTimer(self, eThreadType, iInterval, targetFunction, arrInArgs):
        thread = None
        try:
            self.threadLaunchLock.acquire(True)
            thread = Timer(iInterval, targetFunction, args = arrInArgs)
            thread.start();
        except Exception as e:
            iRetThreadIndex = -1
            log(ERROR, "Unable to launch thread, reason: {0}".format(e))
        finally:
            self.threadLaunchLock.release()

        # log(DEBUG, '<<') # too verbose
        return thread

    def cancelTimer(self, thread):
        log(DEBUG, '>>')
        try:
            self.threadLaunchLock.acquire(True)
            if (thread is not None):
                thread.cancel()
        except:
            log(ERROR, "Unable to cancel timer")
        finally:
            self.threadLaunchLock.release()
        log(DEBUG, '<<')
