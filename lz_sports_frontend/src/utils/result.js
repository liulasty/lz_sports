export const SUCCESS_CODE = 200

export function isSuccess(response) {
  return response?.code === SUCCESS_CODE
}
