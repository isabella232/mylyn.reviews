/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.gerrit.core.remote;

import static org.eclipse.mylyn.internal.gerrit.core.remote.TestRemoteObserverConsumer.retrieveForLocalKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil.PrivilegeLevel;
import org.eclipse.mylyn.gerrit.tests.support.GerritFixture;
import org.eclipse.mylyn.reviews.core.model.IRepository;
import org.eclipse.mylyn.reviews.core.model.IReview;
import org.eclipse.mylyn.reviews.core.model.IReviewItemSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.gerrit.common.data.PatchSetDetail;

public class PatchSetDetailRemoteFactoryTest extends TestCase {

	private static final String NON_DRAFT_BRANCH = "HEAD:refs/for/master";

	private static final String DRAFT_BRANCH = "HEAD:refs/drafts/master";

	private ReviewHarness reviewHarness;

	@Override
	@Before
	public void setUp() throws Exception {
		// sets who is signed-in to view the review (performs the retrieval)
		reviewHarness = new ReviewHarness(System.currentTimeMillis() + "", PrivilegeLevel.USER);
		// set who makes the initial commit (and consequentially, becomes the review owner)
		initializeReviewHarness(DRAFT_BRANCH, PrivilegeLevel.ADMIN);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		reviewHarness.dispose();
	}

	@Test
	public void testUserHasNoAccessToAdminDraft() throws Exception {
		createPatchSet(NON_DRAFT_BRANCH, PrivilegeLevel.ADMIN, ImmutableList.of("testFile2.txt", "testFile3.txt"));

		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();

		assertThat(getReview().getSets().size(), is(1));
		assertThat(getReview().getSets().get(0).getId(), is("2"));

		assertNull(retrievePatchSetDetail("1"));
		PatchSetDetail detail = retrievePatchSetDetail("2");
		assertThat(detail.getInfo().getKey().get(), is(2));
	}

	@Test
	public void testUserHasAccessToAdminDraft() throws Exception {
		createPatchSet(NON_DRAFT_BRANCH, PrivilegeLevel.ADMIN, ImmutableList.of("testFile2.txt", "testFile3.txt"));
		reviewHarness.client.addReviewers(reviewHarness.shortId,
				ImmutableList.of(GerritFixture.current().getCredentials(PrivilegeLevel.USER).getUserName()),
				new NullProgressMonitor());

		reviewHarness.consumer.retrieve(false);
		reviewHarness.listener.waitForResponse();

		assertThat(getReview().getSets().size(), is(2));
		assertThat(getReview().getSets().get(0).getId(), is("1"));
		assertThat(getReview().getSets().get(1).getId(), is("2"));

		PatchSetDetail detail = retrievePatchSetDetail("1");
		assertThat(detail.getInfo().getKey().get(), is(1));
		detail = retrievePatchSetDetail("2");
		assertThat(detail.getInfo().getKey().get(), is(2));

	}

	private void createPatchSet(String pushTo, PrivilegeLevel privilegeLevel, List<String> files) throws Exception {
		CommitCommand command = reviewHarness.createCommitCommand();
		for (String fileName : files) {
			reviewHarness.addFile(fileName);
		}
		reviewHarness.gerritHarness.project().commitAndPush(command, pushTo, privilegeLevel);
	}

	private PatchSetDetail retrievePatchSetDetail(String patchSetId) {
		TestRemoteObserverConsumer<IReview, IReviewItemSet, String, PatchSetDetail, PatchSetDetail, String> itemSetObserver //
		= retrieveForLocalKey(reviewHarness.provider.getReviewItemSetFactory(), getReview(), patchSetId, false);
		PatchSetDetail detail = itemSetObserver.getRemoteObject();
		return detail;
	}

	private void initializeReviewHarness(String refSpec, PrivilegeLevel privilegeLevel) throws Exception {
		reviewHarness.provider.open();
		reviewHarness.pushFileToReview(reviewHarness.testIdent, refSpec, privilegeLevel);
		reviewHarness.listener = new TestRemoteObserver<IRepository, IReview, String, Date>(
				reviewHarness.provider.getReviewFactory());
		reviewHarness.consumer = reviewHarness.provider.getReviewFactory().getConsumerForRemoteKey(
				reviewHarness.getRepository(), reviewHarness.shortId);
		reviewHarness.consumer.addObserver(reviewHarness.listener);
	}

	private IReview getReview() {
		return reviewHarness.consumer.getModelObject();
	}
}
