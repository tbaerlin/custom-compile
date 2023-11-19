/*
 * AbstractRelease.java
 *
 * Created on 28.08.14 14:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tkiesgen
 */
public class AbstractRelease {
    private Map<Long, Assessment> assessments = new HashMap<>(5);

    void addAssessment(Assessment assessment) {
        this.assessments.put(assessment.getType().getRevision(), assessment);
    }

    boolean contains(AssessmentType assessmentType) {
        return this.assessments.containsKey(assessmentType.getRevision());
    }

    Assessment getAssessment(AssessmentType assessmentType) {
        return this.assessments.get(assessmentType.getRevision());
    }

    public List<Assessment> getAssessments() {
        return new ArrayList<>(this.assessments.values());
    }
}
