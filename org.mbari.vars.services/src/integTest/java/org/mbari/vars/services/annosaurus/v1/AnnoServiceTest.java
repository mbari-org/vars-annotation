package org.mbari.vars.services.annosaurus.v1;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.AssertUtils;
import org.mbari.vars.services.TestToolbox;
import org.mbari.vars.services.TestUtils;
import org.mbari.vars.services.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class AnnoServiceTest {

    AnnotationService annoService = TestToolbox.getServices().getAnnotationService();

    private Annotation createRandomAnnotation() {
        var a = TestUtils.buildRandomAnnotation();
        return annoService.createAnnotation(a).join();
    }

    private List<Annotation> createRandomAnnotations(int count, boolean extend) {
        var xs = TestUtils.buildRandomAnnotations(count);
        var ys = annoService.createAnnotations(xs).join();
        if (extend) {
            ys.stream().forEach(y -> {

                // Add association
                var ass = TestUtils.buildRandomAssociation();
                var bss = annoService.createAssociation(y.getObservationUuid(), ass).join();
                y.setAssociations(List.of(bss));

                // Add data
                var d = TestUtils.buildRandomAncillaryData();
                d.setImagedMomentUuid(y.getImagedMomentUuid());
                var e = annoService.createOrUpdateAncillaryData(List.of(d)).join();
                y.setAncillaryData(e.get(0));

                // add image reference
                var i = TestUtils.buildRandomImageReference();
                var j = new Image(y, i);
                var k = annoService.createImage(j).join();
                var ir = new ImageReference(k);
                y.setImageReferences(List.of(ir));
            });
        }
        return new ArrayList<>(ys);
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
        var a = createRandomAnnotation();
        var dt = Duration.ofSeconds(2);
        var t0 = a.getRecordedTimestamp().minus(dt);
        var t1 = a.getRecordedTimestamp().plus(dt);
        var cr = new ConcurrentRequest(t0, t1, List.of(a.getVideoReferenceUuid()));
        var counts = annoService.countByConcurrentRequest(cr).join();
        assertEquals(1L, counts.getCount().longValue());

    }

    @Test
    public void countByMultiRequest() {
        var a = createRandomAnnotation();
        var b = createRandomAnnotation();
        var uuids = List.of(a.getVideoReferenceUuid(), b.getVideoReferenceUuid());
        var mr = new MultiRequest(uuids);
        var counts = annoService.countByMultiRequest(mr).join();
        assertEquals(2L, counts.getCount().longValue());
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
        var a = createRandomAnnotation();
        var ad = TestUtils.buildRandomAncillaryData();

        // Image moment UUID is required
        ad.setImagedMomentUuid(a.getImagedMomentUuid());
        ad.setRecordedTimestamp(a.getRecordedTimestamp());

        // create
        var obtained = annoService.createOrUpdateAncillaryData(List.of(ad)).join();
        assertNotNull(obtained);
        AssertUtils.assertSameAncillaryData(ad, obtained.get(0), false);

        // update
        ad.setLongitude(123.456);
        ad.setLatitude(23.456);
        ad.setDepthMeters(123.456);
        var updated = annoService.createOrUpdateAncillaryData(List.of(ad)).join();
        assertNotNull(updated);
        AssertUtils.assertSameAncillaryData(ad, updated.get(0), false);

    }

    @Test
    public void createCachedVideoReference() {
        var cvr = TestUtils.buildRandomCachedVideoReference();
        var obtained = annoService.createCachedVideoReference(cvr).join();
        assertNotNull(obtained);
        AssertUtils.assertSameCachedVideoReference(cvr ,obtained, false);
    }

    @Test
    public void deleteAncillaryDataByVideoReference() {
        var a = createRandomAnnotation();
        var ad = TestUtils.buildRandomAncillaryData();
        ad.setImagedMomentUuid(a.getImagedMomentUuid());
        ad.setRecordedTimestamp(a.getRecordedTimestamp());
        var obtained = annoService.createOrUpdateAncillaryData(List.of(ad)).join();
        assertNotNull(obtained);
        var count = annoService.deleteAncillaryDataByVideoReference(a.getVideoReferenceUuid()).join();
        assertEquals(1L, count.getCount().longValue());
        assertEquals(a.getVideoReferenceUuid(), count.getVideoReferenceUuid());
        var xs = annoService.findAncillaryDataByVideoReference(a.getVideoReferenceUuid()).join();
        assertTrue(xs.isEmpty());
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
        var a = createRandomAnnotation();
        var b = createRandomAnnotation();
        var uuids = List.of(a.getObservationUuid(), b.getObservationUuid());
        var ok = annoService.deleteAnnotations(uuids).join();
        assertTrue(ok);
        var opt = annoService.findByUuid(a.getObservationUuid()).join();
        assertNull(opt);
        opt = annoService.findByUuid(b.getObservationUuid()).join();
        assertNull(opt);
        assertNull(opt);
    }

    @Test
    public void deleteAssociation() {
        var a = createRandomAnnotation();
        var expected = TestUtils.buildRandomAssociation();
        var obtained = annoService.createAssociation(a.getObservationUuid(), expected).join();
        assertNotNull(obtained);
        var ok = annoService.deleteAssociation(obtained.getUuid()).join();
        assertTrue(ok);
    }

    @Test
    public void deleteAssociations() {
        var a = createRandomAnnotation();
        var xs = List.of(TestUtils.buildRandomAssociation(), TestUtils.buildRandomAssociation());
        var ys = xs.stream()
                .map(x -> annoService.createAssociation(a.getObservationUuid(), x).join())
                .toList();
        var uuids = ys.stream().map(Association::getUuid).toList();
        var ok = annoService.deleteAssociations(uuids).join();
        assertTrue(ok);
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
        var a = TestUtils.buildRandomAnnotation();
        var d = Duration.ofMillis(1234);
        a.setDuration(d);
        var obtained = annoService.createAnnotation(a).join();
        assertNotNull(obtained);
        assertEquals(d, obtained.getDuration());
        var updated = annoService.deleteDuration(obtained.getObservationUuid()).join();
        assert(updated.getDuration() == null);
    }

    @Test
    public void deleteCachedVideoReference() {
        var d = TestUtils.buildRandomCachedVideoReference();
        var obtained = annoService.createCachedVideoReference(d).join();
        assertNotNull(obtained);
        var ok = annoService.deleteCacheVideoReference(obtained.getUuid()).join();
        assertTrue(ok);
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
        var a = createRandomAnnotation();
        var uuids = annoService.findAllVideoReferenceUuids().join();
        assertFalse(uuids.isEmpty());
        assertTrue(uuids.contains(a.getVideoReferenceUuid()));
    }

    @Test
    public void findAncillaryData() {
        var a = createRandomAnnotation();
        var ad = TestUtils.buildRandomAncillaryData();
        ad.setImagedMomentUuid(a.getImagedMomentUuid());
        ad.setRecordedTimestamp(a.getRecordedTimestamp());
        var obtained = annoService.createOrUpdateAncillaryData(List.of(ad)).join();
        assertNotNull(obtained);
        var opt = annoService.findAncillaryData(a.getObservationUuid()).join();
        assertNotNull(opt);
        AssertUtils.assertSameAncillaryData(ad, opt, false);
    }

    @Test
    public void findAncillaryDataByVideoReference() {
        var a = createRandomAnnotation();
        var ad = TestUtils.buildRandomAncillaryData();
        ad.setImagedMomentUuid(a.getImagedMomentUuid());
        ad.setRecordedTimestamp(a.getRecordedTimestamp());
        var obtained = annoService.createOrUpdateAncillaryData(List.of(ad)).join();
        assertNotNull(obtained);
        var xs = annoService.findAncillaryDataByVideoReference(a.getVideoReferenceUuid()).join();
        assertFalse(xs.isEmpty());
        AssertUtils.assertSameAncillaryData(ad, xs.get(0), false);
    }

    @Test
    public void findAnnotations() {

        // simple
        var a = createRandomAnnotation();
        var annotations = annoService.findAnnotations(a.getVideoReferenceUuid()).join();
        assertFalse(annotations.isEmpty());
        AssertUtils.assertSameAnnotation(a, annotations.get(0), true, false);

        // complex
        var xs = createRandomAnnotations(4, true);
        assertEquals(4, xs.size());
        var ys = annoService.findAnnotations(xs.get(0).getVideoReferenceUuid()).join();
        assertEquals(4, ys.size());
        xs.forEach(x ->  assertEquals(1, x.getAssociations().size()));
    }

    @Test
    public void findAnnotations2() {

        // with data
        var xs = createRandomAnnotations(4, true);
        assertEquals(4, xs.size());
        var ys = annoService.findAnnotations(xs.get(0).getVideoReferenceUuid(), true).join();
        assertEquals(4, ys.size());
        xs.forEach(x ->  assertEquals(1, x.getAssociations().size()));
        xs.forEach(x ->  assertNotNull(x.getAncillaryData()));

        // with limit and offset
        var zs = annoService.findAnnotations(xs.get(0).getVideoReferenceUuid(), 2L, 1L, true).join();
        assertEquals(2, zs.size());
        zs.forEach(z ->  assertNotNull(z.getAncillaryData()));

    }


    @Test
    public void findAssociationByUuid() {
        var a = createRandomAnnotations(1, true).get(0);
        var ass = a.getAssociations().get(0);
        var bss = annoService.findAssociationByUuid(ass.getUuid()).join();
        AssertUtils.assertSameAssociation(ass, bss, true);
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
        var a = createRandomAnnotations(1, true).get(0);
        var ir = a.getImageReferences().get(0);
        var xs = annoService.findByImageReference(ir.getUuid()).join();
        assertEquals(1, xs.size());
        var b = xs.get(0);
        AssertUtils.assertSameAnnotation(a, b, true, true);
    }

    @Test
    public void findByMultiRequest() {
        fail("not implemented");
    }

    @Test
    public void findByUuid() {
        var a = createRandomAnnotations(1, true).get(0);
        var b = annoService.findByUuid(a.getObservationUuid()).join();
        AssertUtils.assertSameAnnotation(a, b, true, true);
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