// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

// <ListenerSnippet>
package com.limseong.onenotereminder.util;

import com.limseong.onenotereminder.util.AuthenticationHelper;
import com.microsoft.identity.client.exception.MsalException;

public interface IAuthenticationHelperCreatedListener {
    void onCreated(final AuthenticationHelper authHelper);
    void onError(final MsalException exception);
}
// </ListenerSnippet>