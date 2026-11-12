(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('InterfaceUtilisateur', InterfaceUtilisateur);

    InterfaceUtilisateur.$inject = ['$resource'];

    function InterfaceUtilisateur ($resource) {
        var resourceUrl =  'api/interface-utilisateurs/:id';

        return $resource(resourceUrl, {}, {
            'presentation': { method: 'POST', url: 'api/presentations/',  isArray: false},
            'query': { method: 'GET', isArray: true},
            'interfaceUserFiltered': { method: 'GET', url: 'api/interface-utilisateurs/filtered', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' },
            'duplicate': {   
                method: 'POST', 
                url: 'api/interface-utilisateurs/:id/duplicate'
            }
        });
    }
})();
