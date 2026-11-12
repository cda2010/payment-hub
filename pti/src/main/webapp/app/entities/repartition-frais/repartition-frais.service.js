(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('RepartitionFrais', RepartitionFrais);

    RepartitionFrais.$inject = ['$resource'];

    function RepartitionFrais ($resource) {
        var resourceUrl =  'api/repartition-frais/:id';

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
