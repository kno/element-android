/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.call.dialpad

import im.vector.app.features.call.webrtc.WebRtcCallManager
import im.vector.app.features.createdirect.DirectRoomHelper
import org.matrix.android.sdk.api.extensions.tryOrNull
import org.matrix.android.sdk.api.session.Session
import javax.inject.Inject

class DialPadLookup @Inject constructor(val session: Session,
                                        val directRoomHelper: DirectRoomHelper,
                                        val callManager: WebRtcCallManager
) {

    class Failure : Throwable()
    data class Result(val userId: String, val roomId: String)

    suspend fun lookupPhoneNumber(phoneNumber: String): Result {
        val supportedProtocolKey = callManager.supportedPSTNProtocol ?: throw Failure()
        val thirdPartyUser = tryOrNull {
            session.thirdPartyService().getThirdPartyUser(supportedProtocolKey, fields = mapOf(
                    "m.id.phone" to phoneNumber
            )).firstOrNull()
        } ?: throw Failure()

        val roomId = directRoomHelper.ensureDMExists(thirdPartyUser.userId)
        return Result(userId = thirdPartyUser.userId, roomId = roomId)
    }
}
