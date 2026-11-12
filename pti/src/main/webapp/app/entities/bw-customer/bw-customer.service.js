(function () {
    'use strict';
    angular
            .module('ptiApp')
            .factory('BwCustomer', BwCustomer);

    BwCustomer.$inject = ['$resource'];

    function BwCustomer($resource) {
        var resourceUrl = 'api/bw-customers/:id';

        return $resource(resourceUrl, {}, {
            'queryCust': {method: 'GET', url: 'api/bw-customers/:nom/:prenom/:telephone/:valide', isArray: true},
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': {method: 'PUT'}
        });
    }
})();
