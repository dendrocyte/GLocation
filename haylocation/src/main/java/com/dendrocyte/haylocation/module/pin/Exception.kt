package com.dendrocyte.haylocation.module.pin

/**
 * Created by luyiling on 2024/6/18
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
class PermissionDeniedException(var permissions: Map<String, Boolean>) : Exception()

class GPSUnchangedException : Exception()