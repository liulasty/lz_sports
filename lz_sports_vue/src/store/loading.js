export default {
    state: {
        isLoading: false
    },
    mutations: {
        SET_LOADING(state, isLoading) {
            state.isLoading = isLoading
        }
    }
}
