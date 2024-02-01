package org.mbari.vars.services.annosaurus.v1;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.AssertUtils;
import org.mbari.vars.services.TestToolbox;
import org.mbari.vars.services.TestUtils;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoService;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;

import java.time.Instant;
import java.util.stream.IntStream;

public class AnnoServiceTest {

    AnnotationService annoService = TestToolbox.getServices().getAnnotationService();

    private Annotation createRandomAnnotation() {
        var a = TestUtils.buildRandomAnnotation();
        return annoService.createAnnotation(a).join();
    }

    @Test
    public void createAnnotation() {
        var a = TestUtils.buildRandomAnnotation();
        var obtained = annoService.createAnnotation(a).join();
        assertNotNull(obtained);
        AssertUtils.assertSameAnnotation(a, obtained, false, false);
    }


    @Test
    public void countAnnotations() {
        var a = createRandomAnnotation();
        var count = annoService.countAnnotations(a.getVideoReferenceUuid()).join();
        assertEquals(1L, count.getCount().longValue());
        assertEquals(a.getVideoReferenceUuid(), count.getVideoReferenceUuid());
    }

    @Test
    public void countAnnotationsGroupByVideoReferenceUuid() {
        var a = createRandomAnnotation();
        var counts = annoService.countAnnotationsGroupByVideoReferenceUuid().join();
        var opt = counts.stream().filter(c -> a.getVideoReferenceUuid() == c.getVideoReferenceUuid()).findFirst();
        assertTrue(opt.isPresent());
        var count = opt.get();
        assertEquals(a.getVideoReferenceUuid(), count.getVideoReferenceUuid());
        assertEquals(1L, count.getCount().longValue());

    }

    @Test
    public void findByConcept() {
        var a = createRandomAnnotation();
        var annotations = annoService.findByConcept(a.getConcept(), false).join();
        assertEquals(1, annotations.size());
        AssertUtils.assertSameAnnotation(a, annotations.get(0), true, false);
    }


    @Test
    public void countByConcurrentRequest() {
        fail("not implemented");
    }

    @Test
    public void countByMultiRequest() {
        fail("not implemented");
    }

    @Test
    public void countImagedMomentsGroupByVideoReferenceUuid() {
        var a = createRandomAnnotation();
        var counts = annoService.countImagedMomentsGroupByVideoReferenceUuid().join();
        assert (!counts.isEmpty());
        var opt = counts.stream().filter(c -> a.getVideoReferenceUuid() == c.getVideoReferenceUuid()).findFirst();
        assertTrue(opt.isPresent());
        var count = opt.get();
        assertEquals(a.getVideoReferenceUuid(), count.getVideoReferenceUuid());
        assertEquals(1L, count.getCount().longValue());
    }

    @Test
    public void countObservationsByConcept() {
        var a = createRandomAnnotation();
        var counts = annoService.countObservationsByConcept(a.getConcept()).join();
        assertEquals(1L, counts.getCount().longValue());
        assertEquals(a.getConcept(), counts.getConcept());
    }

    @Test
    public void countImagedMomentsModifiedBefore() {
        var a = createRandomAnnotation();
        var counts = annoService.countImagedMomentsModifiedBefore(a.getVideoReferenceUuid(), Instant.now()).join();
        assertEquals(1L, counts.getCount().longValue());
        assertEquals(a.getVideoReferenceUuid(), counts.getVideoReferenceUuid());
    }

    @Test
    public void createAnnotations() {
        var m = TestUtils.buildRandomMedia();
        var a = IntStream.range(0, 4)
                .mapToObj(i -> TestUtils.buildRandomAnnotation(m))
                .toList();
        var obtained = annoService.createAnnotations(a).join();
        assertNotNull(obtained);
        assertEquals(4, obtained.size());
    }

    @Test
    public void createAssociation() {
        var a = createRandomAnnotation();
        var expected = TestUtils.buildRandomAssociation();
        var obtained = annoService.createAssociation(a.getObservationUuid(), expected).join();
        assertNotNull(obtained);
        AssertUtils.assertSameAssociation(expected, obtained, false);
    }


    @Test
    public void createImage() {
        var a = createRandomAnnotation();
        var imageReference = TestUtils.buildRandomImageReference();
        var expected = new Image(a, imageReference);
        var obtained = annoService.createImage(expected).join();
        assertNotNull(obtained);
        AssertUtils.assertSameImage(expected, obtained, false);
    }

    @Test
    public void createOrUpdateAncillaryData() {
        fail("not implemented");
    }

    @Test
    public void createCachedVideoReference() {
        fail("not implemented");
    }

    @Test
    public void deleteAncillaryDataByVideoReference() {
        fail("not implemented");
    }

    @Test
    public void deleteAnnotation() {
        var a = createRandomAnnotation();
        var ok = annoService.deleteAnnotation(a.getObservationUuid()).join();
        assertTrue(ok);
        var opt = annoService.findByUuid(a.getObservationUuid()).join();
        assertNull(opt);
    }

    @Test
    public void deleteAnnotations() {
        fail("not implemented");
    }

    @Test
    public void deleteAssociation() {
        fail("not implemented");
    }

    @Test
    public void deleteAssociations() {
        fail("not implemented");
    }

    @Test
    public void deleteImage() {
        var a = createRandomAnnotation();
        var imageReference = TestUtils.buildRandomImageReference();
        var expected = new Image(a, imageReference);
        var obtained = annoService.createImage(expected).join();
        assertNotNull(obtained);
        var ok = annoService.deleteImage(obtained.getImageReferenceUuid()).join();
        assertTrue(ok);
        var opt = annoService.findImageByUuid(obtained.getImageReferenceUuid()).join();
        assertNull(opt);
    }

    @Test
    public void deleteDuration() {
        fail("not implemented");
    }

    @Test
    public void deleteCacheVideoReference() {
        fail("not implemented");
    }

    @Test
    public void findActivities() {
        var a = createRandomAnnotation();
        var activities = annoService.findActivities().join();
        assertTrue(!activities.isEmpty());
        assertTrue(activities.contains(a.getActivity()));
    }

    @Test
    public void findAllVideoReferenceUuids() {
        fail("not implemented");
    }

    @Test
    public void findAncillaryData() {
        fail("not implemented");
    }

    @Test
    public void findAncillaryDataByVideoReference() {
        fail("not implemented");
    }

    @Test
    public void findAnnotations() {
        fail("not implemented");
    }

    @Test
    public void testFindAnnotations() {
        fail("not implemented");
    }

    @Test
    public void testFindAnnotations1() {
        fail("not implemented");
    }

    @Test
    public void testFindAnnotations2() {
        fail("not implemented");
    }

    @Test
    public void findAssociationByUuid() {
        fail("not implemented");
    }

    @Test
    public void findByConceptAssociationRequest() {
        fail("not implemented");
    }

    @Test
    public void findByConcurrentRequest() {
        fail("not implemented");
    }

    @Test
    public void findByImageReference() {
        fail("not implemented");
    }

    @Test
    public void findByMultiRequest() {
        fail("not implemented");
    }

    @Test
    public void findByUuid() {
        fail("not implemented");
    }

    @Test
    public void findByVideoReferenceAndLinkName() {
        fail("not implemented");
    }

    @Test
    public void findByVideoReferenceAndLinkNameAndConcept() {
        fail("not implemented");
    }

    @Test
    public void findGroups() {
        var a = createRandomAnnotation();
        var groups = annoService.findGroups().join();
        assertTrue(!groups.isEmpty());
        assertTrue(groups.contains(a.getGroup()));
    }

    @Test
    public void findImageByUrl() {
        fail("not implemented");
    }

    @Test
    public void findImageByUuid() {
        fail("not implemented");
    }

    @Test
    public void findImagesByVideoReferenceUuid() {
        fail("not implemented");
    }

    @Test
    public void findImagedMomentsByVideoReferenceUuid() {
        fail("not implemented");
    }

    @Test
    public void findIndicesByVideoReferenceUuid() {
        fail("not implemented");
    }

    @Test
    public void findVideoReferenceByVideoReferenceUuid() {
        fail("not implemented");
    }

    @Test
    public void merge() {
        fail("not implemented");
    }

    @Test
    public void renameConcepts() {
        fail("not implemented");
    }

    @Test
    public void updateAnnotation() {
        fail("not implemented");
    }

    @Test
    public void updateAnnotations() {
        fail("not implemented");
    }

    @Test
    public void updateAssociation() {
        fail("not implemented");
    }

    @Test
    public void updateAssociations() {
        fail("not implemented");
    }

    @Test
    public void updateImage() {
        fail("not implemented");
    }

    @Test
    public void updateIndexRecordedTimestamps() {
        fail("not implemented");
    }

    @Test
    public void updateRecordedTimestampsForTapes() {
        fail("not implemented");
    }

    @Test
    public void updateRecordedTimestamp() {
        fail("not implemented");
    }

    @Test
    public void updateCachedVideoReference() {
        fail("not implemented");
    }
}