(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TableauSolde', TableauSolde);

    TableauSolde.$inject = ['$resource'];

    function TableauSolde ($resource) {
        var resourceUrl =  'api/tableau-solde/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'getAllSolde': {method: 'GET', url: 'api/allSolde', isArray: false},
            'getDevise': {method: 'GET', url: 'api/found-parametre', isArray: false},
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
