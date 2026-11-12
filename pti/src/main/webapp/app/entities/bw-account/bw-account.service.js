(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('BwAccount', BwAccount);

    BwAccount.$inject = ['$resource'];

    function BwAccount ($resource) {
        var resourceUrl =  'api/bw-accounts/:id';

        return $resource(resourceUrl, {}, {
            'queryAccountCus': { method: 'GET', url:'api/bw-accounts/cus/:cusId', isArray: true},
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
