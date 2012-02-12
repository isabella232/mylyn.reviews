/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package org.eclipse.mylyn.internal.reviews.ui.annotations;

import java.util.List;

import org.eclipse.mylyn.reviews.ui.ReviewBehavior;

/**
 * Data model to represent the annotations that we need to display in the hover.
 * 
 * @author Shawn Minto
 */
public class CommentAnnotationHoverInput {

	private final List<CommentAnnotation> annotations;

	private final ReviewBehavior behavior;

	public CommentAnnotationHoverInput(List<CommentAnnotation> annotations, ReviewBehavior behavior) {
		this.annotations = annotations;
		this.behavior = behavior;
	}

	public boolean containsInput() {
		return annotations != null && annotations.size() > 0;
	}

	public List<CommentAnnotation> getAnnotations() {
		return annotations;
	}

	public ReviewBehavior getBehavior() {
		return behavior;
	}

}
