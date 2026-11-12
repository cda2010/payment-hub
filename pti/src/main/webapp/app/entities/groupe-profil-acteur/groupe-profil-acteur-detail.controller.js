(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('GroupeProfilActeurDetailController', GroupeProfilActeurDetailController);

    GroupeProfilActeurDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'GroupeProfilActeur'];

    function GroupeProfilActeurDetailController($scope, $rootScope, $stateParams, previousState, entity, GroupeProfilActeur) {
        var vm = this;

        vm.groupeProfilActeur = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:groupeProfilActeurUpdate', function(event, result) {
            vm.groupeProfilActeur = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
