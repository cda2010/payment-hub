(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CredentialApiDialogController', CredentialApiDialogController);

    CredentialApiDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'CredentialApi','Notification', 'TypeUOWithoutUR', 'TypeUniteOrganisation', 'UniteOrganisationByType', 'UniteOrganisation', 'RequestUtils'];

    function CredentialApiDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, CredentialApi,Notification, TypeUOWithoutUR, TypeUniteOrganisation, UniteOrganisationByType, UniteOrganisation, RequestUtils) {
        var vm = this;

        vm.credentialApi = entity;
        vm.clear = clear;
        vm.save = save;
        vm.load = load;
        vm.getAuthorities = getAuthorities;
        vm.afterSave=afterSave;
        vm.updateOauth=updateOauth;
        vm.ClientSecret=ClientSecret;
        vm.authoritie=[];
        vm.scopes= [];
        vm.scopesObjet=[];
        vm.grantObjet=[];
        vm.grants=[];
        vm.organisationFSE = [];
        vm.codeTypeUO= null;
        vm.uniteOrganisation= null;
        vm.profils = null;
        function ClientSecret() {
            CredentialApi.getClientSecret(function (reponse) {
                vm.isSaving = false;
                console.log('toto',reponse.clientSecret);
                vm.credentialApi.client_secret= reponse.clientSecret;
            });
            vm.isSaving = false;
        }
        function Scopes(name,title) {
                this.name=name;
                this.title=title;
        }
        function Grant(name,code) {
            this.name=name;
            this.code=code;
        }
        var scope1 = new Scopes('read','r');
        var scope2 = new Scopes('write','w');
        var grant1 = new Grant('password','P');
        var grant2 = new Grant('refresh_token','R');
        var grant3 = new Grant('authorization_code','A');
        var grant4 = new Grant('implicit','I');
        var grant5 = new Grant('client_credentials','C');
        for (let i=0;i<1;i++)
        {
            vm.scopesObjet.push(scope1);
            vm.scopesObjet.push(scope2)

        }
        for (let i=0;i<1;i++)
        {
            vm.grantObjet.push(grant1);
            vm.grantObjet.push(grant2);
            vm.grantObjet.push(grant3);
            vm.grantObjet.push(grant4);
            vm.grantObjet.push(grant5);

        }
        vm.colums = [{
            name: 'name',
            title: 'Code'
        },{
            name: 'libelle',
            title: 'Description'
        }];
        vm.columsScope = [
            {
                name: 'name',
                title: 'titre'
            }
        ];
        vm.columsGrant = [
            {
                name: 'name',
                title: 'code'
            }
        ];
        vm.scopeList = [];
        vm.grantList =[];
        vm.auth = [];
        var i=0;
        angular.forEach(vm.grantObjet,function (value,index) {
            var v = {};
            v.name = value.name;
            i++;
            v.id = i;
            v.title=value.code;
            vm.grantList.push(v);
            vm.grants.push(value.name);
        });
        console.log('liste',vm.grantList);
        angular.forEach(vm.scopesObjet,function (value,index) {
            var v = {};
            v.name = value.name;
            i++;
            v.id = i;
            v.title=value.title;
            vm.scopeList.push(v);
            vm.scopes.push(value.name);
        });
        vm.tesM=[];
        CredentialApi.getAuthoritie(function (response) {
            console.error('Type with out');
            console.log(response);
            var i=0;
            angular.forEach(response,function(value,index){
                var v = {};
                v.name = value.name;
                i++;
                v.id = i;
                v.libelle=value.libelle;
                vm.auth.push(v);
                vm.authoritie.push(value.name);
            });
        });
        $scope.$watch("vm.grantIds", function (ids) {
            console.log('test');
            if (angular.isDefined(ids)) {
                vm.credentialApi.authorized_grant_types = [];
                angular.forEach(ids, function (value, index) {
                    var auth2 = _.find(vm.grantList, { 'id': value });
                    vm.credentialApi.authorized_grant_types.push(auth2.name);
                });
            }
        });
        $scope.$watch("vm.scopeIds", function (ids) {
            if (angular.isDefined(ids)) {
                vm.credentialApi.scope = [];
                angular.forEach(ids, function (value, index) {
                    var auth1 = _.find(vm.scopeList, { 'id': value });
                    vm.credentialApi.scope.push(auth1.name);
                });
            }
        });
        $scope.$watch("vm.authIds", function (ids) {
            if (angular.isDefined(ids)) {
                vm.credentialApi.authorities = [];
                angular.forEach(ids, function (value, index) {
                    var auth = _.find(vm.auth, { 'id': value });
                    vm.credentialApi.authorities.push(auth.name);
                });
            }
        });
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }


        function afterSave() {
                console.log(vm.credentialApi.scope);
                vm.test1 = [];
                vm.test2 = [];
                vm.test3 = [];
                for (let i = 0; i < vm.credentialApi.scope.length; i++) {
                    console.log(vm.credentialApi.scope.length, vm.test1);
                    vm.test1 = vm.credentialApi.scope[i] + ',' + vm.test1;
                }
                for (let i = 0; i < vm.credentialApi.authorities.length; i++) {
                    if (vm.credentialApi.authorities[i]!=='ROLE_USER'){
                        console.log(vm.credentialApi.authorities.length, vm.test2);
                        vm.test2 = vm.credentialApi.authorities[i] + ',' + vm.test2;
                    }
                }
                for (let i = 0; i < vm.credentialApi.authorized_grant_types.length; i++) {
                    console.log(vm.credentialApi.authorized_grant_types.length, vm.test3);
                    vm.test3 = vm.credentialApi.authorized_grant_types[i] + ',' + vm.test3;
                }
                vm.credentialApi.scope = vm.test1.substring(0, vm.test1.length - 1);
                vm.credentialApi.authorities = vm.test2.substring(0, vm.test2.length - 1);
                vm.credentialApi.authorized_grant_types = vm.test3.substring(0, vm.test3.length - 1);

        }
        function updateOauth(id) {
            console.log('---cest moi');
            CredentialApi.getOneOauth({client_id:id},function (result) {
                console.log('-----',result,id);
                vm.credentialApis=result;
            });
        }
        majUserRoles();

        function majUserRoles() {

            if (angular.isDefined(vm.credentialApi) && vm.credentialApi != null) {
                if (vm.credentialApi.client_id != null) {
                    console.log('---bienvenu-',vm.credentialApi);
                    // scope
                    vm.scopeIds = [];
                    vm.credentialApi.scope=vm.credentialApi.scope.split(',');
                    angular.forEach(vm.credentialApi.scope, function (value, index) {
                        var scopeList = _.find(vm.scopeList, { 'name': value });
                        if(angular.isDefined(scopeList)) {
                            vm.scopeIds.push(scopeList.id);
                        }
                    });
                    // authorities
                    CredentialApi.getAuthoritie(function (response) {
                        vm.authIds = [];
                        vm.tesO = [];
                        var i=0;
                        angular.forEach(response,function(value,index){
                            var v = {};
                            v.name = value.name;
                            i++;
                            v.id = i;
                            v.libelle=value.libelle;
                            vm.tesO.push(v);
                        });

                        vm.credentialApi.authorities=vm.credentialApi.authorities.split(',');
                        angular.forEach(vm.credentialApi.authorities, function (value, index) {
                            var auth = _.find(vm.tesO, { 'name': value });
                            console.log(value,vm.tesO,auth);
                            if(angular.isDefined(auth)) {
                                vm.authIds.push(auth.id);
                            }
                        });
                    });
                    // grant
                    vm.grantIds = [];
                    vm.credentialApi.authorized_grant_types=vm.credentialApi.authorized_grant_types.split(',');
                    angular.forEach(vm.credentialApi.authorized_grant_types, function (value, index) {
                        var grantList = _.find(vm.grantList, { 'name': value });
                        if(angular.isDefined(grantList)) {
                            vm.grantIds.push(grantList.id);
                        }
                    });
                }
            }
        }
        vm.isSaving = false;
        function save () {
            vm.isSaving = true;
            afterSave();
            CredentialApi.getOneOauth({client_id:vm.credentialApi.client_id},function (result) {
               console.log('---save response--',result);
               vm.idOauth=result;
                initUO(vm.credentialApi.uniteOrganisation.id);
                initProfil(vm.credentialApi.profilId);
                if (vm.idOauth!=null) {
                    console.log('---update-');
                    CredentialApi.update(vm.credentialApi, onSaveSuccess, onSaveError);
                } else {
                    console.log('---save-');
                    CredentialApi.save(vm.credentialApi, onSaveSuccess, onSaveError);
                }
            });
        }

        function onSaveSuccess (result) {
            Notification.success('Enrégistrer avec succes');
            $scope.$emit('ptiApp:credentialApiUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            Notification.error('Erreur d\'enrégistrement');
            vm.isSaving = false;
        }



        if (angular.isDefined(vm.credentialApi.uniteOrganisation)){
            console.log('---Get UO -', vm.credentialApi.uniteOrganisation.id);
            TypeUniteOrganisation.getOptimise({id : vm.credentialApi.uniteOrganisation.typeUO.id}, function (datas) {
                vm.codeTypeUO = datas.id;
                console.log('Type act',datas.libelleTypeUO);
                load();
            });
        }

        if (angular.isDefined(vm.credentialApi.profil)){
            vm.credentialApi.profilId = vm.credentialApi.profil.id;
        }

        function initUO(uoId) {
            console.log('---Get UO -', uoId);
            UniteOrganisation.queryByIdOptimise({id:uoId}, function (data) {
                vm.credentialApi.uniteOrganisation=data;
            });
        }

        function initProfil(profilId) {
            console.log('---Get profil ', profilId);
            angular.forEach(vm.profils,function(value){
                if (value.id===profilId){
                    vm.credentialApi.profil = value;
                }
            });
        }

        TypeUOWithoutUR.optimise(function (datas) {
            console.log('Type with out', datas);
            vm.typeUniteOrganisations = [];
            angular.forEach(datas,function(value){
                if (value.codeTypeUO==="SOUS_DISTRIBUTEUR" || value.codeTypeUO==="MARCHAND" || value.codeTypeUO==="AGENCE"){
                    vm.typeUniteOrganisations.push(value);
                }
            });
            console.log('TypeUniteOrganisations', vm.typeUniteOrganisations);
        });

        vm.organisationColums = [{
            name: 'libelleUniteOrganisation',
            title: 'Libelle'
        }];

        function load(){
            console.log('codeTypeUO', vm.codeTypeUO);
            if(angular.isDefined(vm.codeTypeUO)){
                TypeUniteOrganisation.getOptimise({id : vm.codeTypeUO}, function (datas) {
                    vm.typeActeur = datas.libelleTypeUO;
                    console.log('type act',vm.typeActeur);
                });
                RequestUtils.ProfilsAndRolesByTypeUOOptimise.query({typeId:vm.codeTypeUO}, function (datas) {
                    vm.profils = datas;
                    console.log('profils',vm.profils);
                    console.log('datas',datas);
                });
                UniteOrganisationByType.queryOptimise({typeId:vm.codeTypeUO}, function (datas) {
                    console.log('Liste Acteurs ',datas);
                    vm.uniteOrganisations = datas;
                });
            }
        }

        function getAuthorities(id) {
            console.log("-------Id ", id);
            if(angular.isDefined(id)){
                console.log("-------isDefined");
                angular.forEach(vm.profils,function(value){
                    console.log("-------value  ", value);
                    if (value.id === id) {
                        let datas = [];
                        angular.forEach(value.authorities,function(v){
                            if (v!=='ROLE_USER'){
                                datas.push(v)
                            }
                        });
                        vm.credentialApi.authorities = datas;
                        console.log("-------Authorities  ", vm.credentialApi.authorities);
                        angular.forEach(vm.credentialApi.authorities, function (value, index) {
                            var auth = _.find(vm.tesO, { 'name': value });
                            console.log(value,vm.tesO,auth);
                            if(angular.isDefined(auth)) {
                                vm.authIds.push(auth.id);
                            }
                        });
                    }
                });
            }
        }


        $scope.$on("onLoadOrganisation", function() {
        });

        $scope.$watch("vm.uniteOrganisations", function(ids) {
            console.log('UO ', ids);
            vm.organisationFSE = ids;
        });


    }
})();
