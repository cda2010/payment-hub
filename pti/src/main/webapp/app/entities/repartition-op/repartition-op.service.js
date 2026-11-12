(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('RepartitionOp', RepartitionOp);

    RepartitionOp.$inject = ['$resource'];

    function RepartitionOp ($resource) {
        var resourceUrl =  'api/repartition-ops/:id';

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
