(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);
    stateConfig.$inject = ['$stateProvider'];
    function stateConfig($stateProvider) {
        $stateProvider.state('account', {
            abstract:true,
            parent:'partial'
        });
    }
})();