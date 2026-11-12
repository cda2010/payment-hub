(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProfilDialogController', ProfilDialogController);

    ProfilDialogController.$inject = ['Principal','TypeUOWithoutUR','RequestUtils','$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Profil', 'UniteOrganisation'];

    function ProfilDialogController (Principal,TypeUOWithoutUR,RequestUtils,$timeout, $scope, $stateParams, $uibModalInstance, entity, Profil, UserExtra, UniteOrganisation) {
        var vm = this;
        vm.profil = entity;
        vm.clear = clear;
        vm.save = save;
        vm.typeuniteorganisations = TypeUOWithoutUR.optimise();
        vm.isAdminG=false;
        vm.profil_authorities=null;
        vm.authorities=[];
        vm.colums = [{
            name: 'name',
            title: 'Code'
        },{
            name: 'libelle',
            title: 'Description'
        }];
        vm.hasRole=function(role){
            return vm.authorities_user.indexOf(role) != -1
        }
        if(vm.profil.id!=null){
            vm.profil_authorities=vm.profil.authorities;
        }
        //Suivi des modification du type de l'uo
         $scope.$watch("vm.profil.typeUOId", function (typeorganisationId) {
             if(angular.isDefined(vm.profil.typeUOId)){
                RequestUtils.GetRolesOfTypeUniteOrganisation.query({id:typeorganisationId}, function (datas) {
                    if (datas != null) {
                        getRoles(datas);
                        majUserRoles();
                    }
                 });
             }
         });

        function getRoles(datas){
            var i=0;
            vm.auth = [];
            angular.forEach(datas,function(value,index){
                var v = {};
                v.name = value.name;
                i++;
                v.id = i;
                v.libelle=value.libelle;
                vm.auth.push(v);
                vm.authorities.push(value.name);
            });
        }

        $scope.$watch("vm.authIds", function (ids) {
            if (angular.isDefined(ids)) {
                console.log('-------------')
                vm.profil.authorities = [];
                angular.forEach(ids, function (value, index) {
                    var auth = _.find(vm.auth, { 'id': value });
                    vm.profil.authorities.push(auth.name);
                });
            }
        });
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        /*
            Comparer deux liste
        */
        function isSame(arrayOne, arrayTwo) {
            var a = arrayOne,
                b = arrayTwo;
            if (arrayOne.length <= arrayTwo.length) {
                a = arrayTwo;
                b = arrayOne;
            }
            return _.isEmpty(_.difference(a.sort(), b.sort()));
        }

        function save () {
            vm.isSaving = true;
            if(vm.profil.id!=null){
                vm.profil.profilsChange=!isSame(vm.profil.authorities,vm.profil_authorities);
            }
            if (vm.profil.id !== null) {
                Profil.update(vm.profil, onSaveSuccess, onSaveError);
            } else {
                Profil.save(vm.profil, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:profilUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        function majUserRoles() {
            if (angular.isDefined(vm.profil) && vm.profil != null) {
                if (vm.profil.id != null) {
                    vm.authIds = [];
                    angular.forEach(vm.profil.authorities, function (value, index) {
                        var auth = _.find(vm.auth, { 'name': value });
                        if(angular.isDefined(auth)) {
                            vm.authIds.push(auth.id);
                        }
                    });
                }
            }
        }

    }
})();
