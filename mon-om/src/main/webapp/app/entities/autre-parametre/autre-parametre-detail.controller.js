(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('AutreParametreDetailController', AutreParametreDetailController);

    AutreParametreDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'AutreParametre', 'FournisseurService'];

    function AutreParametreDetailController($scope, $rootScope, $stateParams, previousState, entity, AutreParametre, FournisseurService) {
        var vm = this;

        vm.autreParametre = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalApp:autreParametreUpdate', function(event, result) {
            vm.autreParametre = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
