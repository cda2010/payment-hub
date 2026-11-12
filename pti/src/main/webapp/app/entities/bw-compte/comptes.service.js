(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Comptes', Comptes);

    Comptes.$inject = ['$resource'];

    function Comptes ($resource) {
        var resourceUrl = 'api/v1/comptes/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'filtredWithCompteBank': { method: 'GET', url: 'api/v1/comptes-with-compte-banque', isArray: true},
            'filtredWithCompteOther': { method: 'GET', url: 'api/v1/comptes-with-compte-other', isArray: true},
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
