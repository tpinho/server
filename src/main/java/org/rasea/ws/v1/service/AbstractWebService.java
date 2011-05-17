package org.rasea.ws.v1.service;

import java.util.List;

import org.jboss.seam.Component;
import org.rasea.core.entity.Application;
import org.rasea.core.entity.Operation;
import org.rasea.core.entity.Permission;
import org.rasea.core.entity.Resource;
import org.rasea.core.exception.DuplicatedException;
import org.rasea.core.exception.EmailSendException;
import org.rasea.core.exception.RequiredException;
import org.rasea.core.exception.StoreException;
import org.rasea.core.service.ApplicationService;
import org.rasea.core.service.AuthorizationService;
import org.rasea.core.service.DataService;
import org.rasea.core.service.MemberService;
import org.rasea.core.service.OperationService;
import org.rasea.core.service.OwnerService;
import org.rasea.core.service.PermissionService;
import org.rasea.core.service.ResourceService;
import org.rasea.core.service.RoleService;
import org.rasea.core.service.UserService;
import org.rasea.extensions.entity.User;
import org.rasea.ws.v1.exception.ExceptionFactory;
import org.rasea.ws.v1.exception.WebServiceException;
import org.rasea.ws.v1.type.CredentialsType;

public abstract class AbstractWebService {

	protected void checkAuthentication(final CredentialsType credentialsType) throws WebServiceException {
		try {
			final boolean authenticated = this.getUserService().authenticate(credentialsType.getUsername(), credentialsType.getPassword());

			if (!authenticated) {
				throw ExceptionFactory.createAuthenticationFailed(credentialsType.getUsername());
			}

		} catch (final Exception cause) {
			this.handleException(cause);
		}
	}

	protected void checkPermission(final CredentialsType credentialsType, final String resourceName, final String operationName) throws WebServiceException {
		try {
			final String username = credentialsType.getUsername();

			final Application application = this.getDefaultApplication();
			final List<Permission> permissions = this.getPermissionService().find(application, new User(username));
			final Resource resource = this.getResource(application, resourceName);
			final Operation operation = this.getOperation(application, operationName);
			final Permission permission = new Permission(resource, operation);

			if (!permissions.contains(permission)) {
				throw ExceptionFactory.createPermissionDenied(username, application.getName(), resourceName, operationName);
			}

		} catch (final Exception cause) {
			this.handleException(cause);
		}
	}

	protected org.rasea.core.entity.Application getApplication(final String application) throws WebServiceException, RequiredException {
		final org.rasea.core.entity.Application app = this.getApplicationService().find(application);

		if (app == null) {
			throw ExceptionFactory.createNotFound("application", application);
		}

		return app;
	}

	protected ApplicationService getApplicationService() {
		return (ApplicationService) Component.getInstance(ApplicationService.class, true);
	}

	protected AuthorizationService getAuthorizationService() {
		return (AuthorizationService) Component.getInstance(AuthorizationService.class, true);
	}

	protected DataService getDataService() {
		return (DataService) Component.getInstance(DataService.class, true);
	}

	protected Application getDefaultApplication() {
		return (Application) Component.getInstance("defaultApplication");
	}

	protected MemberService getMemberService() {
		return (MemberService) Component.getInstance(MemberService.class, true);
	}

	protected org.rasea.core.entity.Operation getOperation(final org.rasea.core.entity.Application aplication, final String operation)
			throws WebServiceException, RequiredException {
		final org.rasea.core.entity.Operation op = this.getOperationService().find(aplication, operation);

		if (op == null) {
			throw ExceptionFactory.createNotFound("operation", operation);
		}

		return op;
	}

	protected OperationService getOperationService() {
		return (OperationService) Component.getInstance(OperationService.class, true);
	}

	protected OwnerService getOwnerService() {
		return (OwnerService) Component.getInstance(OwnerService.class, true);
	}

	protected PermissionService getPermissionService() {
		return (PermissionService) Component.getInstance(PermissionService.class, true);
	}

	protected org.rasea.core.entity.Resource getResource(final org.rasea.core.entity.Application aplication, final String resource)
			throws WebServiceException, RequiredException {
		final org.rasea.core.entity.Resource res = this.getResourceService().find(aplication, resource);

		if (res == null) {
			throw ExceptionFactory.createNotFound("resource", resource);
		}

		return res;
	}

	protected ResourceService getResourceService() {
		return (ResourceService) Component.getInstance(ResourceService.class, true);
	}

	protected org.rasea.core.entity.Role getRole(final org.rasea.core.entity.Application aplication, final String role) throws WebServiceException,
			RequiredException {
		final org.rasea.core.entity.Role serRole = this.getRoleService().find(aplication, role);

		if (serRole == null) {
			throw ExceptionFactory.createNotFound("role", role);
		}

		return serRole;
	}

	protected RoleService getRoleService() {
		return (RoleService) Component.getInstance(RoleService.class, true);
	}

	protected org.rasea.extensions.entity.User getUser(final String username) throws WebServiceException, RequiredException, StoreException {
		final org.rasea.extensions.entity.User usr = this.getUserService().load(username);

		if (usr == null) {
			throw ExceptionFactory.createNotFound("username", username);
		}
		return usr;
	}

	protected UserService getUserService() {
		return (UserService) Component.getInstance(UserService.class, true);
	}

	protected void handleException(final Exception cause) throws WebServiceException {
		if (cause instanceof RequiredException) {
			throw ExceptionFactory.create((RequiredException) cause);

		} else if (cause instanceof DuplicatedException) {
			throw ExceptionFactory.create((DuplicatedException) cause);

		} else if (cause instanceof StoreException) {
			throw ExceptionFactory.create((StoreException) cause);

		} else if (cause instanceof EmailSendException) {
			throw ExceptionFactory.create((EmailSendException) cause);

		} else if (cause instanceof WebServiceException) {
			throw (WebServiceException) cause;

		} else {
			throw ExceptionFactory.createUnknown(cause);
		}
	}
}
