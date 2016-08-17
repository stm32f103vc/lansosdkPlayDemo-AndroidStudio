/************************************************************
 *Email: support@lansongtech.com
 * HWDecUtil.java
 *
 *
 *这个程序仅仅是演示版本，仅是功能上的呈现，不保证性能和适用性。如正好满足您的项目，我们深感荣幸。
 *我们有更专业稳定强大的发行版本，期待和您进一步的合作。
 *
 *Email: support@lansongtech.com
 ************************************************************/
package com.LanSoSdk.Play.Util;

import java.lang.reflect.Method;
import java.util.HashMap;

import com.LanSoSdk.Play.AndroidVersion;

/**
 * Utility class that return the preferred hardware decoder from a list of known devices.
 */
public class HWDecoderUtil {

    public static final boolean HAS_SUBTITLES_SURFACE = AndroidVersion.isGingerbreadOrLater();

    public enum Decoder {
        UNKNOWN, NONE, OMX, MEDIACODEC, ALL
    }

    public enum AudioOutput {
        OPENSLES, AUDIOTRACK, ALL
    }

    private static class DecoderBySOC {
        public final String key;
        public final String value;
        public final Decoder dec;
        public DecoderBySOC(String key, String value, Decoder dec) {
            this.key = key;
            this.value = value;
            this.dec = dec;
        }
    }

    private static class AudioOutputBySOC {
        public final String key;
        public final String value;
        public final AudioOutput aout;

        public AudioOutputBySOC(String key, String value, AudioOutput aout) {
            this.key = key;
            this.value = value;
            this.aout = aout;
        }
    }

    private static final DecoderBySOC[] sBlacklistedDecoderBySOCList = new DecoderBySOC[] {
        /*
         * FIXME: Theses cpu crash in MediaCodec. We need to get hands on these devices in order to debug it.
         */
        new DecoderBySOC("ro.product.board", "msm8916", Decoder.NONE), //Samsung Galaxy Core Prime
        new DecoderBySOC("ro.product.board", "MSM8225", Decoder.NONE), //Samsung Galaxy Core
        new DecoderBySOC("ro.product.board", "hawaii", Decoder.NONE), // Samsung Galaxy Ace 4
    };

    private static final DecoderBySOC[] sDecoderBySOCList = new DecoderBySOC[] {
        /*
         *  Put first devices you want to blacklist
         *  because theses devices can match the next rules.
         */
        new DecoderBySOC("ro.product.brand", "SEMC", Decoder.NONE), // Xperia S
        new DecoderBySOC("ro.board.platform", "msm7627", Decoder.NONE), // QCOM S1

        /*
         * Devices working on OMX
         */
        new DecoderBySOC("ro.board.platform", "omap3", Decoder.OMX), // Omap 3
        new DecoderBySOC("ro.board.platform", "rockchip", Decoder.OMX), // Rockchip RK29
        new DecoderBySOC("ro.board.platform", "rk29", Decoder.OMX), // Rockchip RK29
        new DecoderBySOC("ro.board.platform", "msm7630", Decoder.OMX), // QCOM S2
        new DecoderBySOC("ro.board.platform", "s5pc", Decoder.OMX), // Exynos 3
        new DecoderBySOC("ro.board.platform",  "montblanc", Decoder.OMX), // Montblanc
        new DecoderBySOC("ro.board.platform", "exdroid", Decoder.OMX), // Allwinner A31
        new DecoderBySOC("ro.board.platform", "sun6i", Decoder.OMX), // Allwinner A31

        /*
         * Devices working only on Mediacodec
         */
        new DecoderBySOC("ro.board.platform", "exynos4", Decoder.MEDIACODEC), // Exynos 4 (Samsung Galaxy S2/S3)

        /*
         * Devices working on Mediacodec and OMX
         */
        new DecoderBySOC("ro.board.platform", "omap4", Decoder.ALL), // Omap 4
        new DecoderBySOC("ro.board.platform", "tegra", Decoder.ALL), // Tegra 2 & 3
        new DecoderBySOC("ro.board.platform", "tegra3", Decoder.ALL), // Tegra 3
        new DecoderBySOC("ro.board.platform", "msm8660", Decoder.ALL), // QCOM S3
        new DecoderBySOC("ro.board.platform", "exynos5", Decoder.ALL), // Exynos 5 (Samsung Galaxy S4)
        new DecoderBySOC("ro.board.platform", "rk30", Decoder.ALL), // Rockchip RK30
        new DecoderBySOC("ro.board.platform", "rk31", Decoder.ALL), // Rockchip RK31
        new DecoderBySOC("ro.board.platform", "mv88de3100", Decoder.ALL), // Marvell ARMADA 1500

        new DecoderBySOC("ro.hardware", "mt83", Decoder.ALL), //MTK
    };

    private static final AudioOutputBySOC[] sAudioOutputBySOCList = new AudioOutputBySOC[] {
        /* getPlaybackHeadPosition returns an invalid position on Fire OS,
         * thus Audiotrack is not usable */
        new AudioOutputBySOC("ro.product.brand", "Amazon", AudioOutput.OPENSLES),
    };

    private static final HashMap<String, String> sSystemPropertyMap = new HashMap<String, String>();

    /**
     * @return the hardware decoder known to work for the running device
     * (Always return Dec.ALL after Android 4.3)
     */
    public static Decoder getDecoderFromDevice() {
        /*
         * Try first blacklisted decoders (for all android versions)
         */
        for (DecoderBySOC decBySOC : sBlacklistedDecoderBySOCList) 
        {
            final String prop = getSystemPropertyCached(decBySOC.key);
            if (prop != null) 
            {
                if (prop.contains(decBySOC.value))
                    return decBySOC.dec;  //列表里有这个soc的信息,说明支持该处理器的硬件.
            }
        }
        /*
         * Always try MediaCodec after JellyBean MR2,
         * Try OMX or MediaCodec after HoneyComb depending on device properties.
         * Otherwise, use software decoder by default.
         */
        if (AndroidVersion.isJellyBeanMR2OrLater())
            return Decoder.ALL;
        else if (AndroidVersion.isHoneycombOrLater()) 
        {
            for (DecoderBySOC decBySOC : sDecoderBySOCList) 
            {
                final String prop = getSystemPropertyCached(decBySOC.key);
                if (prop != null) {
                    if (prop.contains(decBySOC.value))
                        return decBySOC.dec;
                }
            }
        }
        return Decoder.UNKNOWN;
    }

    /**
     * @return the audio output known to work for the running device
     * (By default, returns ALL, i.e AudioTrack + OpenSles)
     */
    public static AudioOutput getAudioOutputFromDevice() {
        if (!AndroidVersion.isGingerbreadOrLater()) {
            return AudioOutput.AUDIOTRACK;
        } else {
            for (AudioOutputBySOC aoutBySOC : sAudioOutputBySOCList) {
                final String prop = getSystemPropertyCached(aoutBySOC.key);
                if (prop != null) {
                    if (prop.contains(aoutBySOC.value))
                        return aoutBySOC.aout;
                }
            }
            return AudioOutput.ALL;
        }
    }

    private static String getSystemPropertyCached(String key) {
        String prop = sSystemPropertyMap.get(key);
        if (prop == null) {
            prop = getSystemProperty(key, "none");
            sSystemPropertyMap.put(key, prop);
        }
        return prop;
    }

    //读取系统cpu的信息,和列表中的对比,看是否支持硬件.
    private static String getSystemProperty(String key, String def) {
        try {
            final ClassLoader cl = ClassLoader.getSystemClassLoader();
            final Class<?> SystemProperties = cl.loadClass("android.os.SystemProperties");
            final Class<?>[] paramTypes = new Class[] { String.class, String.class };
            final Method get = SystemProperties.getMethod("get", paramTypes);
            final Object[] params = new Object[] { key, def };
            return (String) get.invoke(SystemProperties, params);
        } catch (Exception e){
            return def;
        }
    }
}
