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

    def launchNewThread(self, eThreadType, targetFunction, arrInArgs):
        log(DEBUG, '>>')
        thread = None
        try:
            self.threadLaunchLock.acquire(True)
            thread = Thread(target = targetFunction, name = "{}".format(self.iThreadIndex), args = arrInArgs)
            thread.start();
        except Exception as e:
            log(ERROR, "Unable to launch thread, reason: {0}".format(e))
        finally:
            self.threadLaunchLock.release()

        log(DEBUG, '<< {}'.format(thread))
        return thread

    def launchTimer(self, eThreadType, fInterval, targetFunction, arrInArgs, bLog = False):
        thread = None
        try:
            fInterval = float(fInterval)
            self.threadLaunchLock.acquire(True)
            thread = Timer(fInterval, targetFunction, args = arrInArgs)
            thread.start();
        except Exception as e:
            log(ERROR, "Unable to launch timer, reason: {}; {} threads running!".format(e, activeCount()))
            thread = None
        finally:
            self.threadLaunchLock.release()
        if bLog:
            log(DEBUG, '<< {} {} seconds'.format(targetFunction, fInterval)) # too verbose
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
