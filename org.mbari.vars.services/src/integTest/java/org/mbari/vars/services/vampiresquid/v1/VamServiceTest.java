package org.mbari.vars.services.vampiresquid.v1;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.services.TestToolbox;
import org.mbari.vars.services.TestUtils;

public class VamServiceTest {

    MediaService mediaService = TestToolbox.getServices().getMediaService();

    @Test
    public void create() {
        var media = TestUtils.createRandomMedia();
        var obtained = mediaService.create(media).join();
        assertNotNull(obtained);
        System.out.println(obtained);
    }

    @Test
    public void testCreate() {
    }

    @Test
    public void update() {
    }

    @Test
    public void testUpdate() {
    }

    @Test
    public void delete() {
    }

    @Test
    public void findByUuid() {
    }

    @Test
    public void findBySha512() {
    }

    @Test
    public void findByUri() {
    }

    @Test
    public void findByVideoSequenceName() {
    }

    @Test
    public void findByVideoName() {
    }

    @Test
    public void findAllVideoSequenceNames() {
    }

    @Test
    public void findByCameraIdAndTimestamp() {
    }

    @Test
    public void findByVideoSequenceNameAndTimestamp() {
    }

    @Test
    public void findByCameraIdAndDate() {
    }

    @Test
    public void findConcurrentByVideoReferenceUuid() {
    }

    @Test
    public void findAllCameraIds() {
    }

    @Test
    public void findVideoSequenceNamesByCameraId() {
    }

    @Test
    public void findVideoNamesByVideoSequenceName() {
    }

    @Test
    public void findByFilename() {
    }

    @Test
    public void findAllURIs() {
    }

    @Test
    public void findLastVideoSequenceUpdate() {
    }

    @Test
    public void findLastVideoUpdate() {
    }

    @Test
    public void findLastVideoReferenceUpdate() {
    }
}