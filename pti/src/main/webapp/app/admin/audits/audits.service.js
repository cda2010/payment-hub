(function() {
    'use strict';

    angular
        .module('ptiApp')
        .factory('AuditsService', AuditsService);

    AuditsService.$inject = ['$resource', 'BASE_URL_API'];

    function AuditsService ($resource, BASE_URL_API) {
        var service = $resource( BASE_URL_API+'management/audits/:id', {}, {
            'get': {
                method: 'GET',
                isArray: true
            },
            'query': {
                method: 'GET',
                isArray: true,
                params: {fromDate: null, toDate: null}
            }
        });

        return service;
    }
})();
