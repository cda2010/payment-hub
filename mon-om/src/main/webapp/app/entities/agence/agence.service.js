(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('Agence', Agence);

    Agence.$inject = ['$resource'];

    function Agence ($resource) {
        var resourceUrl =  'api/agences/:id';

        return $resource(resourceUrl, {}, {
              'queryFournisseur': { method: 'GET', url :'api/agences/fournisseur/:fseId',isArray: true},
            'queryFse': { method: 'GET', url :'api/agences/fse/:fseId',isArray: true},
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
