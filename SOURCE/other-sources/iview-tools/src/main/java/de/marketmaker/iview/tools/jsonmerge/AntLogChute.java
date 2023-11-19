package de.marketmaker.iview.tools.jsonmerge;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

/**
 * Implementation of a logger to output messages via an Ant Task's log
 * method.  Velocity log levels are mapped to corresponding log levels
 * defined in Ant's logging API.  The end result is messages will only
 * be output if Ant log level is high enough.
 *
 * @author    <a href="mailto:billb@progress.com">Bill Burton</a>
 * @version   $Id:$
 */
public class AntLogChute implements LogChute
{

    // Reference to the Ant Task object that initialized the Velocity Engine.
    Task task;

    /**
     * Initialize this logger with a reference to the calling Ant Task
     *
     * @param task Ant Task to use for logging.  This must not be null.
     */
    public AntLogChute(Task task)
    {
        this.task = task;
    }

    /**
     * Initialize the logger.
     *
     * @throws Exception if null was passed into the constructor
     */
    public void init( RuntimeServices rs ) throws Exception
    {
        if ( task == null )
        {
            throw new Exception( "PANIC: " + this.getClass().getName() +
                    " was instantiated with a null Ant Task reference");
        }
    }

    /**
     * <p>
     * Log Velocity messages through the Ant Task log method.  The mapping of logging
     * levels from Velocity to Ant is as follows:
     * </p>
     *
     * <blockquote><pre>
     * Velocity Level      --&gt;  Ant Level
     * LogSystem.TRACE_ID  --&gt;  Project.MSG_DEBUG
     * LogSystem.DEBUG_ID  --&gt;  Project.MSG_DEBUG
     * LogSystem.INFO_ID   --&gt;  Project.MSG_VERBOSE
     * LogSystem.WARN_ID   --&gt;  Project.MSG_WARN
     * LogSystem.ERROR_ID  --&gt;  Project.MSG_ERR
     * </pre></blockquote>
     *
     * @param level    severity level
     * @param message  complete error message
     * @see   org.apache.velocity.runtime.log.LogChute
     * @see   org.apache.tools.ant.Task#log(String, int)
     */
    public void log(int level, String message) {
        switch ( level )
        {
            case LogChute.TRACE_ID:
                task.log( LogChute.TRACE_PREFIX + message, Project.MSG_DEBUG);
                break;
            case LogChute.DEBUG_ID:
                task.log( LogChute.DEBUG_PREFIX + message, Project.MSG_DEBUG );
                break;
            case LogChute.INFO_ID:
                task.log( LogChute.INFO_PREFIX + message, Project.MSG_VERBOSE );
                break;
            case LogChute.WARN_ID:
                task.log( LogChute.WARN_PREFIX + message, Project.MSG_WARN );
                break;
            case LogChute.ERROR_ID:
                task.log( LogChute.ERROR_PREFIX + message, Project.MSG_ERR );
                break;
            default:
                task.log( message );
                break;
        }
    }

    /**
     * <p>
     * Log throwables through the Ant Task log method.  The mapping of logging
     * levels from Velocity to Ant is as follows:
     * </p>
     *
     * <blockquote><pre>
     * Velocity Level      --&gt;  Ant Level
     * LogSystem.TRACE_ID  --&gt;  Project.MSG_DEBUG
     * LogSystem.DEBUG_ID  --&gt;  Project.MSG_DEBUG
     * LogSystem.INFO_ID   --&gt;  Project.MSG_VERBOSE
     * LogSystem.WARN_ID   --&gt;  Project.MSG_WARN
     * LogSystem.ERROR_ID  --&gt;  Project.MSG_ERR
     * </pre></blockquote>
     *
     * @param level    severity level
     * @param message  complete error message
     * @param throwable the throwable object to log
     * @see   org.apache.velocity.runtime.log.LogChute
     * @see   org.apache.tools.ant.Task#log(String, int)
     */
    public void log(int level, String message, Throwable throwable) {
        /*switch ( level )
        {
            case LogChute.TRACE_ID:
                task.log( LogChute.TRACE_PREFIX + message, throwable, Project.MSG_DEBUG);
                break;
            case LogChute.DEBUG_ID:
                task.log( LogChute.DEBUG_PREFIX + message, throwable, Project.MSG_DEBUG );
                break;
            case LogChute.INFO_ID:
                task.log( LogChute.INFO_PREFIX + message, throwable, Project.MSG_VERBOSE );
                break;
            case LogChute.WARN_ID:
                task.log( LogChute.WARN_PREFIX + message, throwable, Project.MSG_WARN );
                break;
            case LogChute.ERROR_ID:
                task.log( LogChute.ERROR_PREFIX + message, throwable, Project.MSG_ERR );
                break;
            default:
                task.log( message );
                break;
        }*/
    }

    public boolean isLevelEnabled(int level) {
        return true;
    }

    public void logVelocityMessage( int level, String message )
    {
    }
}
