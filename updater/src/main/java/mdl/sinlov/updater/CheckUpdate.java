package mdl.sinlov.updater;

/**
 * temp of check update
 * <pre>
 *     sinlov
 *
 *     /\__/\
 *    /`    '\
 *  ≈≈≈ 0  0 ≈≈≈ Hello world!
 *    \  --  /
 *   /        \
 *  /          \
 * |            |
 *  \  ||  ||  /
 *   \_oo__oo_/≡≡≡≡≡≡≡≡o
 *
 * </pre>
 * Created by "sinlov" on 16/6/22.
 */
public class CheckUpdate implements ICheckUpdate{

    private final boolean isForciblyUpdate;
    private final int versionCode;
    private final String updatePackageName;
    private final String updateURL;

    public CheckUpdate(boolean isForciblyUpdate, int versionCode, String updatePackageName, String updateURL) {
        this.isForciblyUpdate = isForciblyUpdate;
        this.versionCode = versionCode;
        this.updatePackageName = updatePackageName;
        this.updateURL = updateURL;
    }

    @Override
    public boolean isForciblyUpdate() {
        return isForciblyUpdate;
    }

    @Override
    public int updateVC() {
        return versionCode;
    }

    @Override
    public String updatePN() {
        return updatePackageName;
    }

    @Override
    public String updateURL() {
        return updateURL;
    }
}
