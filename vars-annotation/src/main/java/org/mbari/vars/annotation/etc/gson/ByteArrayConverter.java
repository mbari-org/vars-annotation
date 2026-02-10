/*
 * @(#)ByteArrayConverter.java   2017.05.15 at 11:00:59 PDT
 *
 * Copyright 2011 MBARI
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.vars.annotation.etc.gson;

import com.google.gson.*;
import org.mbari.vars.annotation.util.HexUtils;

import java.lang.reflect.Type;
import java.util.Base64;

/**
 * @author Brian Schlining
 * @since 2017-05-15T11:00:00
 */
public class ByteArrayConverter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

    /**
     *
     * @param s
     * @return
     */
    public static byte[] decode(String s) {
        return HexUtils.parseHexBinary(s);
    }

    @Override
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return decode(json.getAsString());
    }

    /**
     *
     * @param bs
     * @return
     */
    public static String encode(byte[] bs) {
        return HexUtils.printHexBinary(bs);
    }

    @Override
    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(encode(src));
    }
}
