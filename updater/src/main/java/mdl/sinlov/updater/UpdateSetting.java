package mdl.sinlov.updater;

/**
 * for setting update
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
 * Created by "sinlov" on 16/6/20.
 */
public abstract class UpdateSetting implements IUpdater {

    private ICheckUpdate checkUpdate;

    public ICheckUpdate getCheckUpdate() {
        return checkUpdate;
    }

    public UpdateSetting(ICheckUpdate checkUpdate) {
        this.checkUpdate = checkUpdate;
    }
}
