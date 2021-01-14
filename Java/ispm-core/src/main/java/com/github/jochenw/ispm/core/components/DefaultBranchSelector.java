package com.github.jochenw.ispm.core.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jochenw.afw.core.inject.LogInject;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Strings;


public class DefaultBranchSelector implements IBranchSelector {
	public static class BranchNumber {
		private final String branchName;
		@SuppressWarnings("unused")
		private final String branchNumber;
		private final int[] numbers;
		public BranchNumber(String pBranchName, String pBranchNumber, int[] pNumbers) {
			branchName = pBranchName;
			branchNumber = pBranchNumber;
			numbers = pNumbers;
		}
	}
    public static final Comparator<BranchNumber> BRANCH_NUMBER_COMPARATOR = new Comparator<BranchNumber>() {
		@Override
		public int compare(BranchNumber pV1, BranchNumber pV2) {
			final int[] num1 = pV1.numbers;
			final int[] num2 = pV2.numbers;
			final int length = Math.min(num1.length, num2.length);
			for (int i = 0;  i < length;  i++) {
				final int n = num1[i] - num2[i];
				if (n != 0) {
					return Integer.signum(n);
				}
			}
			final int maxLength = Math.max(num1.length, num2.length);
			for (int i = length;  i < maxLength;  i++) {
				int n1 = num1.length > length ? num1[length] : 0;
				int n2 = num2.length > length ? num2[length] : 0;
				final int n = n1 - n2;
				if (n != 0) {
					return Integer.signum(n);
				}
			}
			return 0;
		}
	};

	private @LogInject ILog log;

	@Override
	public String getBranch(List<String> pBranches) {
		final String mName = "getBranch";
		log.entering(mName, "Branch list", pBranches);
		List<BranchNumber> releaseBranchNumbers = new ArrayList<>();
		final Pattern pattern = Pattern.compile("^remotes/[^/]+/Releases/([\\d\\.]+)$", Pattern.CASE_INSENSITIVE);
		for (String s : pBranches) {
			final Matcher matcher = pattern.matcher(s);
			if (matcher.matches()) {
				final String versionNumber = matcher.group(1);
				final int[] numbers;
				try {
				    numbers = Strings.parseVersionNumber(versionNumber);
				} catch (IllegalArgumentException e) {
					log.warn(mName, "Unable to parse version number from release branch", s);
					continue;
				}
				log.debug(mName, "Found release branch", s);
				releaseBranchNumbers.add(new BranchNumber(s, versionNumber, numbers));
			} else {
				log.debug(mName, "Ignoring non-release branch", s);
			}
		}
		if (releaseBranchNumbers.isEmpty()) {
			log.warn(mName, "No release branch found in list of branch numbers: " + Strings.join(", ", pBranches));
			log.exiting(mName, "null");
			return null;
		} else {
			Collections.sort(releaseBranchNumbers, BRANCH_NUMBER_COMPARATOR);
			final BranchNumber latest = releaseBranchNumbers.get(releaseBranchNumbers.size()-1);
			log.exiting(latest.branchName);
			return latest.branchName;
		}
	}
}
