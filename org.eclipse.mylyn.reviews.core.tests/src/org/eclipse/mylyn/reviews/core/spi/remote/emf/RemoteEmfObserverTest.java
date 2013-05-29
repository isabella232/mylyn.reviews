/*******************************************************************************
 * Copyright (c) 2011 GitHub Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GitHub Inc. - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/
package org.eclipse.mylyn.reviews.core.spi.remote.emf;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.junit.Test;

/**
 * @author Miles Parker
 */
public class RemoteEmfObserverTest {
	@Test
	public void testListeners() {
		TestRemoteFactory factory = new TestRemoteFactory();
		EPackage parent = EcoreFactory.eINSTANCE.createEPackage();
		RemoteEmfConsumer<EPackage, EClass, String, TestRemoteObject, String, Integer> consumer1 = factory.getConsumerForRemoteKey(
				parent, "remoteKeyFor Object 1");
		RemoteEmfConsumer<EPackage, EClass, String, TestRemoteObject, String, Integer> consumer2 = factory.getConsumerForRemoteKey(
				parent, "remoteKeyFor Object 2");
		assertThat(consumer1, not(sameInstance(consumer2)));
		TestRemoteEmfObserver<EPackage, EClass, String, Integer> listener1 = new TestRemoteEmfObserver<EPackage, EClass, String, Integer>(
				consumer1);
		TestRemoteEmfObserver<EPackage, EClass, String, Integer> listener2 = new TestRemoteEmfObserver<EPackage, EClass, String, Integer>();
		consumer1.retrieve(false);
		listener1.waitForResponse(1, 1);
		assertThat(listener2.responded, is(0));
		assertThat(listener2.updated, is(0));
		listener2.setConsumer(consumer1);
		consumer1.retrieve(false);
		listener1.waitForResponse(2, 2);
		assertThat(listener2.responded, is(1));
		assertThat(listener2.updated, is(1));

		consumer1.removeObserver(listener1);
		assertThat(listener1.getConsumer(), nullValue());
		assertThat(listener2.getConsumer() == consumer1, is(true));
		consumer1.retrieve(false);
		listener2.waitForResponse(2, 2);
		assertThat(listener1.responded, is(2));
		assertThat(listener1.updated, is(2));

		consumer2.addObserver(listener1);
		assertThat(listener1.getConsumer() == consumer2, is(true));
		assertThat(listener2.getConsumer() == consumer1, is(true));
		consumer1.retrieve(false);
		listener2.waitForResponse(3, 3);
		assertThat(listener1.responded, is(2));
		assertThat(listener1.updated, is(2));
		consumer2.retrieve(false);
		listener1.waitForResponse(3, 3);
		assertThat(listener1.responded, is(3));
		assertThat(listener1.updated, is(3));

		listener2.setConsumer(consumer2);
		assertThat(listener1.getConsumer() == consumer2, is(true));
		assertThat(listener2.getConsumer() == consumer2, is(true));
		consumer2.retrieve(false);
		listener2.waitForResponse(4, 4);
		assertThat(listener1.responded, is(4));
		assertThat(listener1.updated, is(4));
	}

}