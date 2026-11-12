(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('InstructionComptable', InstructionComptable);

    InstructionComptable.$inject = ['$resource','BASE_URL_API'];

    function InstructionComptable ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'instruction-comptables/:id';

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
