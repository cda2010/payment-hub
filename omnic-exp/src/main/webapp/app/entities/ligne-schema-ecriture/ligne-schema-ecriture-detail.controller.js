(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('LigneSchemaEcritureDetailController', LigneSchemaEcritureDetailController);

    LigneSchemaEcritureDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'LigneSchemaEcriture', 'SchemaEcriture'];

    function LigneSchemaEcritureDetailController($scope, $rootScope, $stateParams, previousState, entity, LigneSchemaEcriture, SchemaEcriture) {
        var vm = this;

        vm.ligneSchemaEcriture = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalPresentationApp:ligneSchemaEcritureUpdate', function(event, result) {
            vm.ligneSchemaEcriture = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
