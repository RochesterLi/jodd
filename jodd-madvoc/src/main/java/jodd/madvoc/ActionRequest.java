// Copyright (c) 2003-2013, Jodd Team (jodd.org). All Rights Reserved.

package jodd.madvoc;

import jodd.madvoc.interceptor.ActionInterceptor;
import jodd.exception.ExceptionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;


/**
 * Encapsulates single action invocation and acts as an action proxy.
 * It invokes all assigned action interceptors during action invocation and
 * specifies the result after action method invocation.
 */
public class ActionRequest {

	protected final ActionConfig config;
	protected final String actionPath;
	protected HttpServletRequest servletRequest;
	protected HttpServletResponse servletResponse;

	protected Object[] params;
	protected final int totalInterceptors;
	protected int interceptorIndex;

	protected Object action;

	protected boolean executed;
	protected String nextActionPath;
	protected ActionRequest previousActionRequest;

	// ---------------------------------------------------------------- accessors

	/**
	 * Returns servlet request.
	 */
	public HttpServletRequest getHttpServletRequest() {
		return servletRequest;
	}

	/**
	 * Specifies new servlet request, in case of wrapping it.
	 */
	public void setHttpServletRequest(HttpServletRequest request) {
		this.servletRequest = request;
	}

	/**
	 * Returns servlet response.
	 */
	public HttpServletResponse getHttpServletResponse() {
		return servletResponse;
	}

	/**
	 * Specifies new servlet response, in case of wrapping it.
	 */
	public void setHttpServletResponse(HttpServletResponse response) {
		this.servletResponse = response;
	}

	/**
	 * Returns {@link ActionConfig action configuration}.
	 */
	public ActionConfig getActionConfig() {
		return config;
	}

	/**
	 * Returns action object.
	 */
	public Object getAction() {
		return action;
	}

	/**
	 * Returns action path.
	 */
	public String getActionPath() {
		return actionPath;
	}

	/**
	 * Returns <code>true</code> if action request was already executed.
	 */
	public boolean isExecuted() {
		return executed;
	}

	/**
	 * Returns next request string for action chaining.
	 */
	public String getNextActionPath() {
		return nextActionPath;
	}

	/**
	 * Specifies the next action path, that will be chained to current action request.
	 */
	public void setNextActionPath(String nextActionPath) {
		this.nextActionPath = nextActionPath;
	}

	/**
	 * Returns previous action request in chain, if there was one.
	 */
	public ActionRequest getPreviousActionRequest() {
		return previousActionRequest;
	}

	/**
	 * Sets previous action request in chain.
	 */
	public void setPreviousActionRequest(ActionRequest previousActionRequest) {
		this.previousActionRequest = previousActionRequest;
	}

	/**
	 * Sets values for action method parameters.
	 */
	public void setActionParams(Object[] params) {
		this.params = params;
	}
	// ---------------------------------------------------------------- ctor

	/**
	 * Creates new action request and action object.
	 */
	public ActionRequest(String actionPath, ActionConfig config, Object action, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		this.actionPath = actionPath;
		this.config = config;
		this.servletRequest = servletRequest;
		this.servletResponse = servletResponse;
		totalInterceptors = (this.config.interceptors != null ? this.config.interceptors.length : 0);
		interceptorIndex = 0;
		this.action = action;
	}


	// ---------------------------------------------------------------- invoke

	/**
	 * Invokes the action and returns action result value object.
	 * Invokes all interceptors before and after action invocation.
	 */
	public Object invoke() throws Exception {
		if (executed == true) {
			throw new MadvocException("Action already invoked: " + config.actionPath);
		}
		// interceptors
		if (interceptorIndex < totalInterceptors) {
			ActionInterceptor interceptor = config.interceptors[interceptorIndex];
			interceptorIndex++;
			return interceptor.invoke(this);
		}

		// action
		Object actionInvocationResult = invokeAction();
		executed = true;
		return actionInvocationResult;
	}

	/**
	 * Invokes action method after starting all interceptors.
	 * After method invocation, all interceptors will finish, in opposite order. 
	 */
	protected Object invokeAction() throws Exception {
		try {
			return config.actionClassMethod.invoke(action, params);
		} catch(InvocationTargetException itex) {
			throw ExceptionUtil.extractTargetException(itex);
		}
	}


}