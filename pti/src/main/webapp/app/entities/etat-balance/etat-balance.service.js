(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Balance', Balance);

    Balance.$inject = ['$resource'];

    function Balance ($resource) {
        var resourceUrl =  'api/black-lists/:id';

        return $resource(resourceUrl, {}, {
            'etat': {method: 'GET',url:'api/etat/balance_cpt/client', isArray: true},
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
