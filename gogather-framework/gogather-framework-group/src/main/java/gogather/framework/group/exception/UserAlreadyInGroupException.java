package gogather.framework.group.exception;

import gogather.framework.core.exception.GoGatherFrameworkException;

public class UserAlreadyInGroupException extends GoGatherFrameworkException {
    public UserAlreadyInGroupException(String message) {
        super(message);
    }
}
