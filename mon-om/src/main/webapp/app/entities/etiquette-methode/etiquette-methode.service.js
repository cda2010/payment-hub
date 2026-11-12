(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('EtiquetteMethode', EtiquetteMethode);

    EtiquetteMethode.$inject = ['$resource'];

    function EtiquetteMethode ($resource) {
        var resourceUrl =  'api/etiquettes-methodes/:id';

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
