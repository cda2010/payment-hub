(function() {
    'use strict';
    angular
        .module('omnicanalPresentationApp')
        .factory('InterfaceUtilisateur', InterfaceUtilisateur);

    InterfaceUtilisateur.$inject = ['$resource'];

    function InterfaceUtilisateur ($resource) {
        var resourceUrl =  'api/interface-utilisateurs/:id';

        return $resource(resourceUrl, {}, {
              'ess1': { method: 'POST', url:'api/boundaryByParam/',  isArray: false  },
            'ess': { method: 'POST', url:'api/omnicanalPresentation/',  isArray: false  },
            'presentation': { method: 'POST', url:'api/presentationsXX/',  isArray: true, trans:function(donnee){
                     if (donnee) {
                        donnee = angular.toJson(donnee);
                    }
                    return donnee;
            }
        },
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
