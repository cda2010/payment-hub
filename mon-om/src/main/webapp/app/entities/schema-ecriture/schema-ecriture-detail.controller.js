(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('SchemaEcritureDetailController', SchemaEcritureDetailController);

    SchemaEcritureDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'SchemaEcriture'];

    function SchemaEcritureDetailController($scope, $rootScope, $stateParams, previousState, entity, SchemaEcriture) {
        var vm = this;

        vm.schemaEcriture = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalApp:schemaEcritureUpdate', function(event, result) {
            vm.schemaEcriture = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
