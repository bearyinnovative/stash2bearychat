package com.pragbits.stash;

import com.atlassian.stash.repository.Repository;
import javax.annotation.Nonnull;

public interface BearyChatSettingsService {

    @Nonnull
    BearyChatSettings getBearyChatSettings(@Nonnull Repository repository);

    @Nonnull
    BearyChatSettings setBearyChatSettings(@Nonnull Repository repository, @Nonnull BearyChatSettings settings);

}
