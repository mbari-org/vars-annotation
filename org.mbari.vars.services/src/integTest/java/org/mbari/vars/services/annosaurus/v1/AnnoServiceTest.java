package org.mbari.vars.services.annosaurus.v1;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mbari.vars.core.util.StringUtils;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.AssertUtils;
import org.mbari.vars.services.TestToolbox;
import org.mbari.vars.services.TestUtils;
import org.mbari.vars.services.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
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
        var xs = createRandomAnnotations(4, true);
        var a = xs.get(0);
        var ass = a.getAssociations().get(0);
        var linkName = ass.getLinkName();
        var vru = xs.stream().map(Annotation::getVideoReferenceUuid).toList();
        var car = new ConceptAssociationRequest(linkName, vru);
        var obtained = annoService.findByConceptAssociationRequest(car).join();
        var c = obtained.getConceptAssociationRequest();
        assertEquals(c.getLinkName(), linkName);
        assertEquals(c.getVideoReferenceUuids(), vru);
        var results = obtained.getConceptAssociations();
        assertEquals(results.size(), 1);
        var ca = results.get(0);
        assertEquals(ca.getToConcept(), ass.getToConcept());
        assertEquals(ca.getLinkName(), ass.getLinkName());
        assertEquals(ca.getLinkValue(), ass.getLinkValue());
        assertEquals(ca.getConcept(), a.getConcept());
        assertEquals(ca.getMimeType(), ass.getMimeType());
        assertEquals(ca.getUuid(), ass.getUuid());
        assertEquals(ca.getVideoReferenceUuid(), a.getVideoReferenceUuid());
    }

    @Test
    public void findByConcurrentRequest() {
        var xs = createRandomAnnotations(5,  false)
                .stream()
                .sorted(Comparator.comparing(Annotation::getConcept))
                .toList();
        var uuids = xs.stream().map(Annotation::getVideoReferenceUuid).toList();
        var tx = xs.stream().map(Annotation::getRecordedTimestamp).toList();
        var dt = Duration.ofMillis(1000);
        var t0 = tx.stream()
                .min(Comparator.comparing(Instant::toEpochMilli))
                .map(i -> i.minus(dt))
                .get();
        var t1 = tx.stream()
                .max(Comparator.comparing(Instant::toEpochMilli))
                .map(i -> i.plus(dt))
                .get();

        var cr = new ConcurrentRequest(t0, t1, uuids);
        var obtained = annoService.findByConcurrentRequest(cr, 100, 0)
                .join()
                .stream()
                .sorted(Comparator.comparing(Annotation::getConcept))
                .toList();
        assertNotNull(obtained);
        assertEquals( xs.size(), obtained.size());
        for (int i = 0; i < xs.size(); i++) {
            var a = xs.get(i);
            var b = obtained.get(i);
            AssertUtils.assertSameAnnotation(a, b, true, true);
        }
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
        var xs = createRandomAnnotations(2,  false);
        var ys = createRandomAnnotations(2, true);
        var annos = new ArrayList<>(xs);
        annos.addAll(ys);
        var uuids = annos.stream().map(Annotation::getVideoReferenceUuid).toList();

        var expected = annos.stream()
                .sorted(Comparator.comparing(Annotation::getConcept))
                .toList();


        var mr = new MultiRequest(uuids);

        var obtained = annoService.findByMultiRequest(mr, 100, 0)
                .join()
                .stream()
                .sorted(Comparator.comparing(Annotation::getConcept))
                .toList();
        assertNotNull(obtained);
        assertEquals( expected.size(), obtained.size());
        for (int i = 0; i < expected.size(); i++) {
            var a = expected.get(i);
            var b = obtained.get(i);
            AssertUtils.assertSameAnnotation(a, b, true, true);
        }
    }

    @Test
    public void findByUuid() {
        var a = createRandomAnnotations(1, true).get(0);
        var b = annoService.findByUuid(a.getObservationUuid()).join();
        AssertUtils.assertSameAnnotation(a, b, true, true);
    }

    @Test
    public void findByVideoReferenceAndLinkName() {
        var xs = createRandomAnnotations(2, true);
        var a = xs.get(0);
        var expected = a.getAssociations().get(0);
        var ys = annoService.findByVideoReferenceAndLinkName(a.getVideoReferenceUuid(), expected.getLinkName()).join();
        assertEquals(1, ys.size());
        var obtained = ys.get(0);
        AssertUtils.assertSameAssociation(expected, obtained, true);
    }

    @Test
    public void findByVideoReferenceAndLinkNameAndConcept() {
        var xs = createRandomAnnotations(2, true);
        var a = xs.get(0);
        var expected = a.getAssociations().get(0);
        var ys = annoService.findByVideoReferenceAndLinkNameAndConcept(a.getVideoReferenceUuid(), expected.getLinkName(), a.getConcept()).join();
        assertEquals(1, ys.size());
        var obtained = ys.get(0);
        AssertUtils.assertSameAssociation(expected, obtained, true);
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
        var a = createRandomAnnotations(1, true).get(0);
        var ir = a.getImageReferences().get(0);
        var expected = new Image(a, ir);
        var obtained = annoService.findImageByUuid(ir.getUuid()).join();
        assertNotNull(obtained);
        AssertUtils.assertSameImage(expected, obtained, true);
    }

    @Test
    public void findImagesByVideoReferenceUuid() {
        var xs = createRandomAnnotations(2, true);
        var expected = xs.stream()
                .map(a -> {
                    var ir = a.getImageReferences().get(0);
                    return new Image(a, ir);
                })
                .sorted(Comparator.comparing(Image::getImageReferenceUuid))
                .toList();
        var obtained = annoService.findImagesByVideoReferenceUuid(xs.get(0).getVideoReferenceUuid()).join()
                .stream()
                .sorted(Comparator.comparing(Image::getImageReferenceUuid))
                .toList();

        for (int i = 0; i < expected.size(); i++) {
            var a = expected.get(i);
            var b = obtained.get(i);
            AssertUtils.assertSameImage(a, b, true);
        }

    }

    @Test
    public void findImagedMomentsByVideoReferenceUuid() {
        var xs = createRandomAnnotations(2, true);
        var expected = xs.stream()
                .map(Annotation::getImagedMomentUuid)
                .sorted()
                .toList();
        var obtained = annoService.findImagedMomentsByVideoReferenceUuid(xs.get(0).getVideoReferenceUuid()).join();
        assertEquals(expected.size(), obtained.size());
    }

    @Test
    public void findIndicesByVideoReferenceUuid() {
        var xs = createRandomAnnotations(2, true);
        var expected = xs.stream()
                .map(Annotation::getImagedMomentUuid)
                .sorted()
                .toList();
        var obtained = annoService.findIndicesByVideoReferenceUuid(xs.get(0).getVideoReferenceUuid()).join();
        assertEquals(expected.size(), obtained.size());
    }

    @Test
    public void findVideoReferenceByVideoReferenceUuid() {
        var a = TestUtils.buildRandomCachedVideoReference();
        var b = annoService.createCachedVideoReference(a).join();
        var c = annoService.findVideoReferenceByVideoReferenceUuid(b.getVideoReferenceUuid()).join();
        AssertUtils.assertSameCachedVideoReference(b, c, true);
    }

    @Test
    public void merge() {
        fail("not implemented");
    }

    @Test
    public void renameConcepts() {
        var oldConcept = StringUtils.random(12);
        var newConcept = StringUtils.random(12);
        var xs = TestUtils.buildRandomAnnotations(2)
                .stream()
                .peek(a -> a.setConcept(oldConcept))
                .sorted(Comparator.comparing(Annotation::getConcept))
                .toList();
        var ys = annoService.createAnnotations(xs).join();
        assertEquals(xs.size(), ys.size());
        var cr = annoService.renameConcepts(oldConcept, newConcept).join();
        assertEquals(2L, cr.getCount().longValue());
        assertEquals(oldConcept, cr.getOldConcept());
        assertEquals(newConcept, cr.getNewConcept());
    }

    @Test
    public void updateAnnotation() {
        var a = createRandomAnnotation();
        a.setConcept(StringUtils.random(12));

        var b = annoService.updateAnnotation(a).join();
        a.setObservationTimestamp(b.getObservationTimestamp());
        AssertUtils.assertSameAnnotation(a, b, true, true);
    }

    @Test
    public void updateAnnotations() {
        var xs = createRandomAnnotations(2, true);
        var ys = xs.stream()
                .peek(a -> a.setConcept(StringUtils.random(12)))
                .sorted(Comparator.comparing(Annotation::getConcept))
                .toList();
        var zs = annoService.updateAnnotations(ys)
                .join()
                .stream()
                .sorted(Comparator.comparing(Annotation::getConcept))
                .toList();
        assertEquals(ys.size(), zs.size());
        for (int i = 0; i < ys.size(); i++) {
            var a = ys.get(i);
            var b = zs.get(i);
            a.setObservationTimestamp(b.getObservationTimestamp());
            AssertUtils.assertSameAnnotation(a, b, true, true);
        }
    }

    @Test
    public void updateAssociation() {
        var a = createRandomAnnotations(1, true).get(0);
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