(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('RepartionFraisEmAcq', RepartionFraisEmAcq);

    RepartionFraisEmAcq.$inject = ['$resource','BASE_URL_API'];

    function RepartionFraisEmAcq ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'repartion-frais-em-acqs/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
